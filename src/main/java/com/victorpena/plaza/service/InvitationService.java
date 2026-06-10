package com.victorpena.plaza.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.model.TenantInvitation;
import com.victorpena.plaza.repository.TenantInvitationRepository;
import com.victorpena.plaza.repository.UserRepository;

@Service
public class InvitationService {

    private final TenantInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public InvitationService(
            TenantInvitationRepository invitationRepository,
            UserRepository userRepository,
            JavaMailSender mailSender) {

        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    public TenantInvitation getValidInvitation(String token) {

        TenantInvitation invitation =
                invitationRepository.findByToken(token)
                        .orElse(null);

        if (invitation == null) {
            return null;
        }

        if (invitation.isUsed()) {
            return null;
        }

        if (invitation.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            return null;
        }

        return invitation;
    }

    public void markUsed(TenantInvitation invitation) {

        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }

    public void sendInvitation(Lease lease) {

        String email = lease.getTenantEmail();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Lease must have a tenant email.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "A user with this email already exists.");
        }

        Optional<TenantInvitation> existingInvite =
                invitationRepository.findByLeaseIdAndUsedFalse(
                        lease.getId());

        if (existingInvite.isPresent()
                && existingInvite.get()
                        .getExpiresAt()
                        .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "An active invitation already exists for this lease.");
        }

        TenantInvitation invitation = new TenantInvitation();

        invitation.setLease(lease);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(
                LocalDateTime.now().plusDays(7));
        invitation.setUsed(false);

        invitationRepository.save(invitation);

        String registrationLink =
                "http://localhost:8080/register?token="
                        + invitation.getToken();

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom("13victor.pena@gmail.com");
        message.setTo(email);

        message.setSubject(
                "Peña Plaza Tenant Registration");

        message.setText(
                "You have been invited to register for Peña Plaza.\n\n"
                        + "Click the link below to create your account:\n\n"
                        + registrationLink);

        mailSender.send(message);
    }
}