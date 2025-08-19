package timesheet.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
	@Autowired
	private NotificationRepository notificationRepository;

	@GetMapping("/unread/{username}")
	public List<Notificationdao> getUnreadNotifications(@PathVariable String username) {
		return notificationRepository.findByUsernameAndReadedFalse(username);
	}

	@PostMapping("/markRead/{id}")
	public ResponseEntity<?> markAsRead(@PathVariable Long id) {
		Optional<Notificationdao> notification = notificationRepository.findById(id);
		if (notification.isPresent()) {
			Notificationdao n = notification.get();
			n.setReaded(true);
			notificationRepository.save(n);
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.notFound().build();
	}
}
