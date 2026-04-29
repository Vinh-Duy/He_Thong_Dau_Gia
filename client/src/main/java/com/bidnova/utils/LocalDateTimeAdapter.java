package com.bidnova.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

// adapter: extend TypeAdapter<LocalDateTime> 
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        return LocalDateTime.parse(in.nextString(), FORMATTER);
    }

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        out.value(value.format(FORMATTER)); // Chuyển LocalDateTime thành String rồi ghi
    }
}
