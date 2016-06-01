package com.test.projects.disruptor;

import com.lmax.disruptor.EventHandler;

public class DataDealHandler  implements EventHandler<DataFetchEvent> {

	@Override
	public void onEvent(DataFetchEvent event, long sequence, boolean endOfBatch) throws Exception {
		 System.out.println("数据处理线程("+Thread.currentThread().getId()+")：orderId="+event.getValue()+" sequence="+sequence+" end="+endOfBatch);
	}

}
