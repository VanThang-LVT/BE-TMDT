package com.lvt.tmdt;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class TmdtApplication {

	public static void main(String[] args) {
		SpringApplication.run(TmdtApplication.class, args);
	}

	@Bean
	public boolean forceFlyway(DataSource dataSource) {
		System.out.println("====== ĐANG ÉP FLYWAY CHẠY ======");
		Flyway flyway = Flyway.configure()
				.dataSource(dataSource)
				.baselineOnMigrate(true)
				.baselineVersion("1")
				.locations("classpath:db/migration")
				.load();

		flyway.migrate();
		System.out.println("====== FLYWAY ĐÃ CHẠY XONG ======");
		return true;
	}

}
