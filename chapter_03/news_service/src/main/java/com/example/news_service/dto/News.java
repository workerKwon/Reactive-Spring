package com.example.news_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class News {

    @Id
    @JsonIgnore
    private ObjectId id;

    private @NonNull String title;
    private @NonNull String content;
    private @NonNull Date publishedOn;
    private @NonNull String category;
    private @NonNull String author;
}
