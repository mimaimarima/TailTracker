/**
 * 
 */
/**
 * 
 */
module mvPets {
	exports pets.model;
	exports pets.model.enums;
	exports pets.config;
	requires transitive java.sql;
	requires org.junit.jupiter.api;
	requires jbcrypt;
	requires com.h2database;
	requires jdk.httpserver;
	opens pets.model to org.junit.platform.commons;
}