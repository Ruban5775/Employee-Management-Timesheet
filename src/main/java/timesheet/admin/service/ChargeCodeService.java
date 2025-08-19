package timesheet.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import timesheet.admin.dao.ChargeCodeCounter;
import timesheet.admin.repo.ChargeCountRepo;

@Service
public class ChargeCodeService {

	@Autowired
	private ChargeCountRepo chargeCodeCounterRepository;

	@PostConstruct
	public void initializeCounterIfNeeded() {
		chargeCodeCounterRepository.initializeCounter();
	}

	public int getNextCodeIncrement() {

		ChargeCodeCounter counter = chargeCodeCounterRepository.findById(1)
				.orElseThrow(() -> new RuntimeException("Counter not found"));

		int lastIncrement = counter.getLastIncrement();

		lastIncrement++;

		counter.setLastIncrement(lastIncrement);
		chargeCodeCounterRepository.save(counter);

		return lastIncrement;
	}
}
