#!/bin/bash

set -e

# Define colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration variables
JMETER_HOME="/opt/jmeter"  # Modify to match your JMeter installation path
MASTER_IP=$(hostname -I | awk '{print $1}')
SLAVE_IPS="192.168.1.100:1099,192.168.1.101:1099,192.168.1.102:1099"
RESULTS_BASE_DIR="./performance-test/jmeter/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_DIR="$RESULTS_BASE_DIR/$TIMESTAMP"

# Define functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check JMeter installation
if [ ! -f "$JMETER_HOME/bin/jmeter.sh" ]; then
    log_error "JMeter가 $JMETER_HOME 에 설치되어 있지 않습니다."
    log_info "JMeter 설치 후 JMETER_HOME 경로를 수정해주세요."
    exit 1
fi

# Create results directory
mkdir -p "$RESULTS_DIR"

log_info "JMeter 분산 테스트 Master 서버 설정"
echo "=================================="
echo "Master IP: $MASTER_IP"
echo "Slave 목록: $SLAVE_IPS"
echo "결과 저장: $RESULTS_DIR"
echo "JMeter Home: $JMETER_HOME"
echo "=================================="

# Check Slave server connectivity
log_info "Slave 서버 연결 상태 확인 중..."
for slave in $(echo $SLAVE_IPS | tr "," "\n"); do
    ip=$(echo $slave | cut -d':' -f1)
    port=$(echo $slave | cut -d':' -f2)

    if nc -z $ip $port 2>/dev/null; then
        log_success "$slave - 연결 성공"
    else
        log_warning "$slave - 연결 실패 (나중에 연결될 수 있음)"
    fi
done

# Set JMeter environment variables
export HEAP="-Xms2g -Xmx4g"
export JVM_ARGS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Djava.rmi.server.hostname=$MASTER_IP"

# Copy JMeter properties file
cp "./performance-test/jmeter/distributed/master-config/jmeter.properties" "$JMETER_HOME/bin/"

log_info "JMeter Master 서버가 준비되었습니다."
log_info "분산 테스트를 시작하려면 다음 명령어를 사용하세요:"
echo ""
echo "# 기본 테스트 실행"
echo "$JMETER_HOME/bin/jmeter.sh -n -t your-test-plan.jmx -R $SLAVE_IPS -l $RESULTS_DIR/results.jtl"
echo ""
echo "# HTML 리포트와 함께 실행"
echo "$JMETER_HOME/bin/jmeter.sh -n -t your-test-plan.jmx -R $SLAVE_IPS -l $RESULTS_DIR/results.jtl -e -o $RESULTS_DIR/html-report"
echo ""

# Automatic test execution option
read -p "지금 바로 테스트를 실행하시겠습니까? (y/N): " choice
case "$choice" in
  y|Y )
    if [ -f "./performance-test/jmeter/scenarios/routine-performance-test.jmx" ]; then
        log_info "성능 테스트를 시작합니다..."

        $JMETER_HOME/bin/jmeter.sh \
            -n \
            -t "./performance-test/jmeter/scenarios/routine-performance-test.jmx" \
            -R "$SLAVE_IPS" \
            -l "$RESULTS_DIR/results.jtl" \
            -e \
            -o "$RESULTS_DIR/html-report" \
            -Jthreads=300 \
            -Jramp-up=120 \
            -Jduration=600

        log_success "테스트 완료! 결과: $RESULTS_DIR"
    else
        log_warning "테스트 플랜 파일이 없습니다. 먼저 JMX 파일을 생성해주세요."
    fi
    ;;
  * )
    log_info "수동으로 테스트를 실행해주세요."
    ;;
esac