package com.example.news_service;

import com.example.news_service.dto.News;
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
import ratpack.jackson.Jackson;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

import java.util.Arrays;
import java.util.Date;

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

	public static void main(String[] args) throws Exception {
		SpringApplication.run(NewsServiceApplication.class, args);

		RatpackServer.start(ratpackServerSpec ->
				ratpackServerSpec.serverConfig(ServerConfig.embedded().port(NEWS_SERVER_PORT))
						.handlers(chain -> chain.get(context -> context.render(Jackson.json(Arrays.asList(
								News.builder()
										.author("kwon")
										.category("kwon")
										.content("kwon")
										.publishedOn(new Date())
										.title("kwon")
										.build(),
								News.builder()
										.author("tae")
										.category("tae")
										.content("tae")
										.publishedOn(new Date())
										.title("tae")
										.build(),
								News.builder()
										.author("park")
										.category("park")
										.content("park")
										.publishedOn(new Date())
										.title("park")
										.build(),
								News.builder()
										.author("kim")
										.category("kim")
										.content("kim")
										.publishedOn(new Date())
										.title("kim")
										.build(),
								News.builder()
										.author("lee")
										.category("lee")
										.content("lee")
										.publishedOn(new Date())
										.title("lee")
										.build(),
								News.builder()
										.author("hae")
										.category("hae")
										.content("hae")
										.publishedOn(new Date())
										.title("hae")
										.build(),
								News.builder()
										.author("jung")
										.category("jung")
										.content("jung")
										.publishedOn(new Date())
										.title("jung")
										.build()
						)))))
		);
	}

	private interface DatabaseNewsService {
		FindPublisher<News> lookupNews();
	}

}
