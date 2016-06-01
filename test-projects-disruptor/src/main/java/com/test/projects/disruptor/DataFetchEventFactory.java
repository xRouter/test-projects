package com.test.projects.disruptor;

import com.lmax.disruptor.EventFactory;

public class DataFetchEventFactory implements EventFactory<DataFetchEvent>{

	@Override
	public DataFetchEvent newInstance() {
		return new DataFetchEvent();
	}
}
