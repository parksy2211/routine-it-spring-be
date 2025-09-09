### 서브 캘린더 생성

- https://developers.kakao.com/docs/latest/ko/talkcalendar/rest-api#calendar-create-sub
    - 회원 가입시 서브캘린더가 생성됨.
        - name = “rountine-it for group”,  color = “LIME”, reminder = 10

### 서브 캘린더 삭제

- https://developers.kakao.com/docs/latest/ko/talkcalendar/rest-api#calendar-delete-sub
    - 회원 탈퇴시 서브 캘린더 삭제

### 일정 생성

- https://developers.kakao.com/docs/latest/ko/talkcalendar/rest-api#common-event-create
    - 유저별 서브 캘린더에 일정 생성
        - 그룹 가입 후 그룹멤버의 상태가 JOINED일때 생성됨
        - 그룹이름과 그룹설명, alarmTiem, authDays(ex “0101010” == 월수금이 실행날짜)과 연동
        - 일정의 반복 주기는 기본 한 달.

### 일정 수정

- https://developers.kakao.com/docs/latest/ko/talkcalendar/rest-api#common-event-modify
    - 그룹 정보가 업데이트 될 때 그에 맞게 일정이 업데이트  됨
        - recur_update_type = “THIS_AND_FOLLOWING”

### 일정 삭제

- https://developers.kakao.com/docs/latest/ko/talkcalendar/rest-api#common-event-delete
    - 유저가 그룹에 나가 그룹멤버 상태가 JOINED가 아닌경우 일정이 삭제됨
    - 유저가 가입된 그룹이 삭제된 경우 일정이 삭제됨


캘린더의 조회 기능은 이 애플리케이션엔 필요없는 기능임.
이 앱에서 캘린더나 일정 내용을 저장해서 보여줄 계획이 아님.
단지 카카오 캘린더와 연동하여 루틴일정을 카카오 캘런더에서 볼 수 있게 하기 위함.