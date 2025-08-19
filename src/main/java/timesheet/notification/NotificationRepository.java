package timesheet.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notificationdao, Long> {
	List<Notificationdao> findByUsernameAndReadedFalse(String username);
}
