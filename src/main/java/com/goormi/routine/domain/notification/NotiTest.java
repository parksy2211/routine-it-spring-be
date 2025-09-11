//package com.goormi.routine.domain.notification;
//
//import com.goormi.routine.domain.notification.repository.NotificationRepository;
//import com.goormi.routine.domain.user.entity.User;
//import com.goormi.routine.domain.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Profile("local") // 로컬 테스트에서만 활성화하도록 설정
//public class NotiTest {
//
//    private final NotificationRepository notificationRepository;
//    private final UserRepository userRepository;
////    private final Faker faker = new Faker();
//
//
//
//    @Transactional
//    public void insertBulkData(int totalSize, int batchSize) {
//        List<User> users = new ArrayList<>();
//        long id = 0L;
//        for (int i = 1; i <= totalSize; i++) {
//            users.add(createRandomEntity(++id));
//
//            if (i % batchSize == 0 || i == totalSize) {
//                userRepository.saveAll(users);
//                users.clear(); // 메모리 관리를 위한 초기화
//                System.out.println("Inserted " + i + " records...");
//            }
//        }
//    }
//
//    private User createRandomEntity(long kakaoId) {
////        String email = faker.;
////        String name = faker.name().fullName();
////        String content = faker.lorem().sentence();
////        return User.createKakaoUser(kakaoid, email, name, content); // 엔티티 생성자에 맞게 조정
//    }
//
//
//}
