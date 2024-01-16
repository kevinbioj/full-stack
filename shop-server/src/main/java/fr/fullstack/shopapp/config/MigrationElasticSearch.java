package fr.fullstack.shopapp.config;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MigrationElasticSearch implements CommandLineRunner {

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("--- start migration ---");
        migrateData();
        System.out.println("--- end migration ---");
    }

    private void migrateData() {
        SearchSession searchSession = Search.session(entityManager);
        searchSession
            .massIndexer()
            .purgeAllOnStart(true)
            .start();
    }
}
