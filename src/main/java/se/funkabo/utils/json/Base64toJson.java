package se.funkabo.utils.json;

import java.lang.reflect.Type;
import java.util.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Base64toJson implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

	@Override
	public byte[] deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		return Base64.getDecoder().decode(json.getAsString());
	}

	@Override
	public JsonElement serialize(byte[] src, Type type, JsonSerializationContext ctx) {
		return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
	}

}

