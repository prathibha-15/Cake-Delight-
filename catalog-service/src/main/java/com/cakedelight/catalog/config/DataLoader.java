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
                        "https://kreamz.in/celebration-cake/chocolate-truffle-cake/"
                ));

                repository.save(new Cake(
                        null,
                        "Black Forest",
                        "Classic black forest",
                        "Birthday",
                        699.0,
                        15,
                        "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=900&q=80"
                ));

                repository.save(new Cake(
                        null,
                        "Red Velvet",
                        "Soft red velvet cake",
                        "Anniversary",
                        899.0,
                        12,
                        "https://images.unsplash.com/photo-1586985289906-406988f2fbe4?auto=format&fit=crop&w=900&q=80"
                ));

                repository.save(new Cake(
                        null,
                        "Vanilla Delight",
                        "Classic vanilla cake",
                        "Birthday",
                        599.0,
                        18,
                        "https://images.unsplash.com/photo-1565958011703-44f9829ba187?auto=format&fit=crop&w=900&q=80"
                ));

                repository.save(new Cake(
                        null,
                        "Butterscotch",
                        "Crunchy butterscotch cake",
                        "Birthday",
                        749.0,
                        10,
                        "https://images.unsplash.com/photo-1576119191902-3e3ac6fe1f3e?auto=format&fit=crop&w=900&q=80"
                ));

                repository.save(new Cake(
                        null,
                        "Blueberry Cheesecake",
                        "Creamy blueberry cheesecake",
                        "Cheesecake",
                        650.0,
                        8,
                        "https://images.unsplash.com/photo-1559620192-032c4bc4674e?auto=format&fit=crop&w=900&q=80"
                ));

                repository.save(new Cake(
                        null,
                        "Strawberry Shortcake",
                        "Light strawberry sponge layered cake",
                        "Dessert",
                        560.0,
                        14,
                        "https://images.unsplash.com/photo-1567529695817-3f7b4fbc5d0c?auto=format&fit=crop&w=900&q=80"
                ));

                repository.save(new Cake(
                        null,
                        "Oreo Celebration",
                        "Cream-filled Oreo style celebration cake",
                        "Celebration",
                        880.0,
                        9,
                        "https://images.unsplash.com/photo-1614707267537-7c4981f2f0f0?auto=format&fit=crop&w=900&q=80"
                ));

                repository.save(new Cake(
                        null,
                        "Classic Pineapple",
                        "A fresh pineapple sponge cake",
                        "Fruit",
                        620.0,
                        11,
                        "https://images.unsplash.com/photo-1571115177098-24ec42ed204d?auto=format&fit=crop&w=900&q=80"
                ));

                repository.save(new Cake(
                        null,
                        "Creamy Pistachio",
                        "A rich pistachio cream celebration cake",
                        "Celebration",
                        720.0,
                        13,
                        "https://images.unsplash.com/photo-1562440499-64c9a111f713?auto=format&fit=crop&w=900&q=80"
                ));

                System.out.println("Inserted 10 sample cakes successfully.");

            } else {
                System.out.println("Database already contains data. Skipping sample data.");
            }
        };
    }
}