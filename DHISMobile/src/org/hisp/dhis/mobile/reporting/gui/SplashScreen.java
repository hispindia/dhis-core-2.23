/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.hisp.dhis.mobile.reporting.gui;

import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStoreException;

import org.hisp.dhis.mobile.reporting.db.SettingsRecordStore;

/**
 * 
 * @author abyotag_adm
 */
public class SplashScreen extends Canvas {

	private Display display;

	private Displayable loginForm;

	private Displayable pinForm;

	private Image image;

	private Timer timer = new Timer();

	public SplashScreen(Image image, Display display, Displayable loginForm,
			Displayable pinForm) {
		this.image = image;
		this.display = display;
		this.loginForm = loginForm;
		this.pinForm = pinForm;
		display.setCurrent(this);
	}

	protected void keyPressed(int keyCode) {
		dismissSplashScreen();
	}

	protected void paint(Graphics g) {
		g.setColor(66, 80, 115);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (image != null)
			g.drawImage(image, getWidth() / 2, getHeight() / 2,
					Graphics.HCENTER | Graphics.VCENTER);
	}

	protected void pointerPressed(int x, int y) {
		dismissSplashScreen();
	}

	protected void showNotify() {
		timer.schedule(new CountDown(), 3000);
	}

	private void dismissSplashScreen() {
		timer.cancel();
		SettingsRecordStore settingStore = null;

		try {
			settingStore = new SettingsRecordStore(
					SettingsRecordStore.SETTINGS_DB);
			if (settingStore.get("pin").equals("")) {
				display.setCurrent(loginForm);
			} else {
				display.setCurrent(pinForm);
			}
		} catch (RecordStoreException e) {
			e.printStackTrace();
		}
	}

	// count down for the splash display
	private class CountDown extends TimerTask {
		public void run() {

			dismissSplashScreen();

		}
	}
}
