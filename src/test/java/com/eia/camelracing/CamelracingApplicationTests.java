package com.eia.camelracing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Sin @ActiveProfiles("test") este smoke test arranca con el perfil "dev"
// (el que declara application.yml por defecto) y necesita un PostgreSQL
// real en localhost:5432 -> justo lo que el resto de la suite evita a
// propósito usando H2 en memoria, para poder correr sin Docker levantado.
@SpringBootTest
@ActiveProfiles("test")
class CamelracingApplicationTests {

	@Test
	void contextLoads() {
	}

}
