package com.sportshop.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Stable JSON wrapper for paginated results (avoids serializing PageImpl directly).
 */
public class PageResponse<T> {
	private List<T> content;
	private long totalElements;
	private int totalPages;
	private int number;
	private int size;
	private boolean first;
	private boolean last;
	private int numberOfElements;

	public static <T> PageResponse<T> of(Page<T> page) {
		PageResponse<T> response = new PageResponse<>();
		response.content = page.getContent();
		response.totalElements = page.getTotalElements();
		response.totalPages = page.getTotalPages();
		response.number = page.getNumber();
		response.size = page.getSize();
		response.first = page.isFirst();
		response.last = page.isLast();
		response.numberOfElements = page.getNumberOfElements();
		return response;
	}

	public List<T> getContent() {
		return content;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public int getNumberOfElements() {
		return numberOfElements;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}
}
