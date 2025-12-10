package com.amigoscode;

import com.amigoscode.customer.Customer;
import com.amigoscode.customer.CustomerRepository;
import com.amigoscode.customer.Gender;
import com.amigoscode.s3.S3Service;
import com.amigoscode.s3.S3Buckets;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            S3Service s3Service,
            S3Buckets s3Buckets) {

        return args -> {
            // create random customer
            createRandomCustomer(customerRepository, passwordEncoder);

            // test S3 upload + download
            testBucketUploadAndDownload(s3Service, s3Buckets);
        };
    }

    // ✅ this must be OUTSIDE the runner
    public static void testBucketUploadAndDownload(
            S3Service s3Service,
            S3Buckets s3Buckets) {

        s3Service.putObject(
                s3Buckets.getCustomer(),
                "secret file shhhh",
                "Madame Morrible MM".getBytes()
        );

        byte[] obj = s3Service.getObject(
                s3Buckets.getCustomer(),
                "secret file shhhh"
        );

        System.out.println("file contents: " + new String(obj));
    }

    private static void createRandomCustomer(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {

        var faker = new Faker();
        Random random = new Random();

        Name name = faker.name();
        String firstName = name.firstName();
        String lastName = name.lastName();

        int age = random.nextInt(16, 99);
        Gender gender = age % 2 == 0 ? Gender.MALE : Gender.FEMALE;
        String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@amigoscode.com";

        Customer customer = new Customer(
                firstName + " " + lastName,
                email,
                passwordEncoder.encode("password"),
                age,
                gender
        );

        customerRepository.save(customer);
        System.out.println("Created customer: " + email);
    }
}
