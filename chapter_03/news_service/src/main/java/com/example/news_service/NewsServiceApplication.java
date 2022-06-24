package com.example.news_service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.connection.netty.NettyStreamFactory;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import rx.Observable;

@SpringBootApplication
public class NewsServiceApplication {

	public static final int NEWS_SERVER_PORT = 8070;

	@Autowired
	MongoClient mongoClient;

	@Bean
	MongoClient mongoClient(MongoProperties properties) {
		ConnectionString connectionString = new ConnectionString(properties.determineUri());
		MongoClientSettings.Builder mongoBuilder = MongoClientSettings.builder()
				.streamFactoryFactory(NettyStreamFactory::new)
				.applyToClusterSettings(builder -> builder.applyConnectionString(connectionString))
				.applyToConnectionPoolSettings(builder -> builder.applyConnectionString(connectionString))
				.applyToServerSettings(builder -> builder.applyConnectionString(connectionString))
				.applyToSslSettings(builder -> builder.applyConnectionString(connectionString))
				.applyToSocketSettings(builder -> builder.applyConnectionString(connectionString))
				.codecRegistry(CodecRegistries.fromRegistries(
						MongoClients.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(
								PojoCodecProvider.builder()
										.automatic(true)
										.register(News.class)
										.build()
						)));

		if (connectionString.getReadPreference() != null) {
			mongoBuilder.readPreference(connectionString.getReadPreference());
		}
		if (connectionString.getReadConcern() != null) {
			mongoBuilder.readConcern(connectionString.getReadConcern());
		}
		if (connectionString.getWriteConcern() != null) {
			mongoBuilder.writeConcern(connectionString.getWriteConcern());
		}
		if (connectionString.getApplicationName() != null) {
			mongoBuilder.applicationName(connectionString.getApplicationName());
		}

		return MongoClients.create(mongoBuilder.build());
	}

	@Bean
	DatabaseNewsService databaseNews() {
		return () -> mongoClient.getDatabase("news")
				.getCollection("news")
				.find(News.class)
				.sort(Sorts.descending("publishedOn"))
				.filter(Filters.eq("category", "tech"));
	}

	public static void main(String[] args) {
		SpringApplication.run(NewsServiceApplication.class, args);
	}

	private interface DatabaseNewsService {
		FindPublisher<News> lookupNews();
	}

}
