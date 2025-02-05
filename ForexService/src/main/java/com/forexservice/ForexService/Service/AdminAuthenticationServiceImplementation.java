package com.forexservice.ForexService.Service;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.forexservice.ForexService.Entity.Admin;
import com.forexservice.ForexService.Entity.AdminVerification;
import com.forexservice.ForexService.Exception.AdminAuthenticationFailureException;
import com.forexservice.ForexService.Exception.AdminEmailNotExisting;
import com.forexservice.ForexService.Exception.AdminNotFoundException;
import com.forexservice.ForexService.Repository.AdminRepository;
import com.forexservice.ForexService.Repository.AdminVerificationRepository;
@Service
public class AdminAuthenticationServiceImplementation implements AdminAuthenticationService{
	

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired 
	private AdminRepository adminRepository;
	
	@Autowired
	private AdminVerificationRepository verificationRepository;
	
	
	@Override
	public Admin doLogin(String adminUsername, String adminPassword) {
		
		Optional<Admin> optionalAdmin = adminRepository.login(adminUsername, adminPassword);
		
		if(optionalAdmin.isEmpty()) {
			throw new AdminNotFoundException("Admin not existing");
		}
		
		Admin admin=optionalAdmin.get();
		
		if(!adminPassword.equals(admin.getAdminPassword())) {
			
			throw new AdminAuthenticationFailureException("login failed");
			
		}
		
		return admin;
	}
	
	
	 @Override
		public String sendEmail(String toEmail) {
			
			Optional<Admin> admin= adminRepository.findByAdminEmail(toEmail);
			if(admin.isPresent())
			{
				  SimpleMailMessage message = new SimpleMailMessage();		
				    message.setTo(toEmail);
		            message.setSubject("Email Verification for  Application");
				    String code = RandomStringUtils.randomNumeric(4);
		            message.setText(code);
		            javaMailSender.send(message);
			        AdminVerification verification=new AdminVerification();
			      
			        verification.setAdminEmail(toEmail);
			        verification.setOtp(code);
			        verificationRepository.save(verification);
			        return "mail sent successfully";

		    	}
		    	else {
		    		throw new AdminAuthenticationFailureException ("enter Valid Email");
		    		}
				
			  }


		@Override
		public String verifyOtp(String adminEmail, String otp) {
			Optional<AdminVerification> obj= verificationRepository.findByAdminEmail(adminEmail);
			
					if(obj.isPresent() ) {
					AdminVerification obj1=obj.get();
								if(obj1.getOtp().equals(otp)) 
								{
									verificationRepository.delete(obj1);
									return "otp matched";
								}
								else {
								throw new AdminEmailNotExisting("Otp did not matched");
								}
	    			
						}
					else {
						throw new AdminEmailNotExisting("Enter Valid Email ID");
					}	
			
		
		
		}



		@Override
		public String verifyEmail(String adminEmail) {
			Optional<Admin> admin=adminRepository.findByAdminEmail(adminEmail);
			if(admin.isEmpty())
			{
				return "Admin Not Registered with this Email Id";
			}
			else
			{
				return null;
			}
		}	
	

}
