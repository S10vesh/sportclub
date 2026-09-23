package ru.sportclub;

import ru.sportclub.repository.MemberRepository;
import ru.sportclub.repository.TrainingRegistrationRepository;
import ru.sportclub.service.MemberService;
import ru.sportclub.service.TrainingRegistrationService;
import ru.sportclub.ui.ConsoleApplication;
import ru.sportclub.util.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        DatabaseManager database = new DatabaseManager();
        MemberRepository memberRepository = new MemberRepository(database);
        TrainingRegistrationRepository registrationRepository = new TrainingRegistrationRepository(database);
        MemberService memberService = new MemberService(memberRepository);
        TrainingRegistrationService registrationService = new TrainingRegistrationService(registrationRepository, memberRepository);
        new ConsoleApplication(memberService, registrationService).run();
    }
}
