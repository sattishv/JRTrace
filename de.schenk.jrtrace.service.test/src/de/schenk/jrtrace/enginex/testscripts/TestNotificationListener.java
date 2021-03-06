/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.Notification;

import de.schenk.jrtrace.service.NotificationAndErrorListener;

public class TestNotificationListener extends NotificationAndErrorListener {

	@Override
	public void sendMessage(Notification notification) {
		if (notificationBarrier == null)
			return;
		lastNotification = notification;

		try {
			notificationBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException("test");
		}
		notificationBarrier = null;

	}

	CyclicBarrier notificationBarrier = new CyclicBarrier(2);
	Notification lastNotification = null;

	/**
	 * waits 10 seconds to get an error message and will return the message or
	 * null if no message was received in time
	 * 
	 * @return
	 */
	public Notification waitForNotification() {

		try {
			notificationBarrier.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return null;
		} catch (BrokenBarrierException e) {
			return null;
		} catch (TimeoutException e) {
			return null;
		}
		return lastNotification;
	}

	public void reset() {
		lastNotification = null;
		notificationBarrier = new CyclicBarrier(2);

	}
}
