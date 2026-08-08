package com.cakedelight.catalog.config;

import com.cakedelight.catalog.entity.Cake;
import com.cakedelight.catalog.repository.CakeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(CakeRepository repository) {

        return args -> {

            System.out.println("========== DataLoader Started ==========");

            long count = repository.count();
            System.out.println("Current Cake Count : " + count);

            if (count == 0) {

                repository.save(new Cake(
                        null,
                        "Chocolate Truffle",
                        "Rich chocolate cake",
                        "Birthday",
                        799.0,
                        20,
                        "https://example.com/chocolate.jpg"
                ));

                repository.save(new Cake(
                        null,
                        "Black Forest",
                        "Classic black forest",
                        "Birthday",
                        699.0,
                        15,
                        "https://example.com/blackforest.jpg"
                ));

                repository.save(new Cake(
                        null,
                        "Red Velvet",
                        "Soft red velvet cake",
                        "Anniversary",
                        899.0,
                        12,
                        "https://example.com/redvelvet.jpg"
                ));

                repository.save(new Cake(
                        null,
                        "Vanilla Delight",
                        "Classic vanilla cake",
                        "Birthday",
                        599.0,
                        18,
                        "https://example.com/vanilla.jpg"
                ));

                repository.save(new Cake(
                        null,
                        "Butterscotch",
                        "Crunchy butterscotch cake",
                        "Birthday",
                        749.0,
                        10,
                        "https://example.com/butterscotch.jpg"
                ));

                repository.save(new Cake(
                        null,
                        "Blueberry Cheesecake",
                        "Creamy blueberry cheesecake",
                        "Cheesecake",
                        650.0,
                        8,
                        "https://example.com/blueberry.jpg"
                ));

                repository.save(new Cake(
                        null,
                        "Strawberry Shortcake",
                        "Light strawberry sponge layered cake",
                        "Dessert",
                        560.0,
                        14,
                        "https://example.com/strawberry.jpg"
                ));

                repository.save(new Cake(
                        null,
                        "Oreo Celebration",
                        "Cream-filled Oreo style celebration cake",
                        "Celebration",
                        880.0,
                        9,
                        "https://example.com/oreo.jpg"
                ));

                repository.save(new Cake(
                        null,
                        "Classic Pineapple",
                        "A fresh pineapple sponge cake",
                        "Fruit",
                        620.0,
                        11,
                        "https://example.com/pineapple.jpg"
                ));

                System.out.println("Inserted 10 sample cakes successfully.");

            } else {
                System.out.println("Database already contains data. Skipping sample data.");
            }
        };
    }
}