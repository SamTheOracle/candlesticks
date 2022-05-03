package com.oracolo.cloud.events;

public class QuoteData {
	public double price;
	public String isin;

	@Override
	public String toString() {
		return "QuoteData{" + "price=" + price + ", isin='" + isin + '\'' + '}';
	}
}
