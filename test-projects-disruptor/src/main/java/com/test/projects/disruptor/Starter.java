package com.test.projects.disruptor;

import java.util.concurrent.Executors;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class Starter {

	public static void main(String[] args) throws InterruptedException {
		Disruptor<DataFetchEvent> disruptor = new Disruptor<DataFetchEvent>(new DataFetchEventFactory(), 1024, Executors.newCachedThreadPool()	, ProducerType.SINGLE, new SleepingWaitStrategy());
		//要设置自己的EventHandler，处理业务
		DataDealHandler[] arrays = new DataDealHandler[4];
		arrays[0] = new DataDealHandler();
		arrays[1] = new DataDealHandler();
		arrays[2] = new DataDealHandler();
		arrays[3] = new DataDealHandler();
		disruptor.handleEventsWith(arrays);
		disruptor.start();
		
		RingBuffer<DataFetchEvent> ringBuffer = disruptor.getRingBuffer();
		for(int j=0;j<100;j++){
			//批量取数据20条; 查询业务
			System.out.println("开始第："+(j+1)+"批次！");
			int batchNum = 20;
			long maxSeq = ringBuffer.next(batchNum);
			System.out.println("maxSeq:"+maxSeq);
			for(int i = batchNum-1; i >= 0 ;i--){
				long curSeq = maxSeq-i;
				System.out.println("curSeq:"+curSeq);
				DataFetchEvent event = ringBuffer.get(curSeq);
				event.setValue(curSeq);
			}
			//ringBuffer.next(batchNum); 每次获取，序列号都会增加，和是否publish没关系
			ringBuffer.publish(maxSeq);
			System.out.println("maxSeq"+maxSeq+" published");
			Thread.sleep(1000);
		}
		
	}
}
