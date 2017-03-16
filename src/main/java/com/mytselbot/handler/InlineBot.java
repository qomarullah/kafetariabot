package com.mytselbot.handler;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.telegram.database.DatabaseTsel;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardHide;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.mytselbot.BotConfig;
import com.mytselbot.BotLogger;
import com.mytselbot.BuildVars;
import com.mytselbot.util.Emoji;

public class InlineBot extends TelegramLongPollingBot {
	private static final String LOGTAG = "Kafetaria {}";
	
	ArrayList<HashMap<String, String>> menu = new ArrayList<HashMap<String, String>>();

	String order = Emoji.HAPPY_PERSON_RAISING_ONE_HAND + " Kafetaria";
	String cari = Emoji.FACE_WITH_COLD_SWEAT + " Cari";
	String promo = Emoji.SPARKLES + " Promo";
	String feedback = Emoji.SPARKLES + " Feedback";

	HashMap<String, String> userState = null;
	static ArrayList<HashMap<String, String>> menulist = new ArrayList<HashMap<String, String>>();
	
	public InlineBot() {
		
		HashMap<String, String> menu1 = new HashMap<String, String>();
		menu1.put("menu", order);
		HashMap<String, String> menu2 = new HashMap<String, String>();
		menu2.put("menu", cari);
		HashMap<String, String> menu3 = new HashMap<String, String>();
		menu3.put("menu", promo);
		HashMap<String, String> menu4 = new HashMap<String, String>();
		menu4.put("menu", feedback);
		HashMap<String, String> menu5 = new HashMap<String, String>();

		menu.add(menu1);
		//menu.add(menu2);
		// menu.add(menu3);
		menu.add(menu4);

	}

	private boolean registered = false;

	@Override
	public void onUpdateReceived(Update update) {
		try {
			BotLogger.debug(LOGTAG, update.toString());
			if (update.hasMessage()) {
				Message message = update.getMessage();
				if (message.hasText() || message.hasLocation()) {
					handleIncomingMessage(message);
				} else if (null != message.getContact().getPhoneNumber()) {

					if (message.getContact().getUserID().equals(message.getFrom().getId())) {
						DatabaseTsel.getInstance().putMemberPhone(message.getContact().getUserID(),
								message.getContact().getPhoneNumber(), message.getFrom().getUserName(),
								message.getContact().getFirstName());

						registered = true;
					
						//send main menu
						String username = getUsername(message);
						sendMainMenu(message, username, menu);

					} else {
						String text = Emoji.TIRED_FACE.toString() + "Maaf silakan daftarkan nomor dengan benar";
						sendRegisterMessage(message, text);

					}

				}
			} else if (update.hasCallbackQuery()) {
				BotLogger.debug(LOGTAG, "ada-callback");
				handleIncomingCallbackQuery(update.getCallbackQuery());
			}

		} catch (Exception e) {
			BotLogger.error(LOGTAG, e);
		}
	}

	private void sendReplyMessageHTML(Message message,Integer userId, Long chatId, Integer messageId, String text)
			throws TelegramApiException {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId.toString());
		sendMessage.enableHtml(true);
		if(message.getChat().isGroupChat()){
			sendMessage.setReplayToMessageId(messageId);
		}
		sendMessage.setText(text);
		

		ReplyKeyboardHide replyKeyboardHide = new ReplyKeyboardHide();
		replyKeyboardHide.setSelective(true);
		sendMessage.setReplayMarkup(replyKeyboardHide);

		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getMenuHeader(String text) {
		String resp = ""+Emoji.BLACK_SUN_WITH_RAYS + Emoji.BLACK_SUN_WITH_RAYS + Emoji.BLACK_SUN_WITH_RAYS
						+ text
						+ Emoji.BLACK_SUN_WITH_RAYS + Emoji.BLACK_SUN_WITH_RAYS + Emoji.BLACK_SUN_WITH_RAYS;

		return resp;

	}
	
	private void getMenuOrder(Message message,Integer userId, Long chatId, Integer messageId, String caption, String search)
			throws TelegramApiException {
		
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId.toString());
		sendMessage.enableHtml(true);
		if(message.getChat().isGroupChat()){
			sendMessage.setReplayToMessageId(messageId);
		}
		String text ="";
		if(search.equals("")){
			text = getMenuHeader("Menu Hari Ini") + "\n";
			menulist = DatabaseTsel.getInstance().getMenu();
		}else if(!search.equals("")){
			text = getMenuHeader("Hasil Pencarian Menu") + "\n";
			menulist = DatabaseTsel.getInstance().getMenuBySearch(search);
		}
		
		NumberFormat formatter = new DecimalFormat("#0.##");
				
		for (int i = 0; i < menulist.size(); i++) {
			String number = menulist.get(i).get("number");
			String merchant=menulist.get(i).get("merchant");
			String name=menulist.get(i).get("name");
			double price=Double.parseDouble(menulist.get(i).get("price"));
			price=(double)price/1000;
			
			if(!merchant.equals("")){
				merchant=" ("+merchant+")";
			}else if(merchant.equals("kosong")){
				merchant="";
			}

			text += "\n" + number + "." + name+ " "+formatter.format(price)+"rb" + merchant;
			
		}
		if(menulist.size()>0){
			text += "\n\n" + caption;
		}else{
			text += "Maaf hasil pencarian tidak ditemukan";
		}
		sendMessage.setText(text);

		ReplyKeyboardHide replyKeyboardHide = new ReplyKeyboardHide();
		replyKeyboardHide.setSelective(true);
		sendMessage.setReplayMarkup(replyKeyboardHide);

		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void getMenuOrderInline(Message message,Integer userId, Long chatId, Integer messageId, String caption, String search)
			throws TelegramApiException {
		

		SendMessage sendMessage = new SendMessage();
		
		sendMessage.setChatId(chatId.toString());
		sendMessage.enableHtml(true);
		sendMessage.setText("Silakan klik menu dibawah:");

		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

		List<List<InlineKeyboardButton>> keyboard = new ArrayList<List<InlineKeyboardButton>>();
		List<InlineKeyboardButton> keyboard1 = new ArrayList<InlineKeyboardButton>();
		
		menu = DatabaseTsel.getInstance().getMenu();
		
		NumberFormat formatter = new DecimalFormat("#0.##");
		
		InlineKeyboardButton btn = new InlineKeyboardButton();
		for (int i = 0; i < menu.size(); i++) {
			String number = menu.get(i).get("number");
			String merchant=menu.get(i).get("merchant");
			String name=menu.get(i).get("name");
			double price=Double.parseDouble(menu.get(i).get("price"));
			price=(double)price/1000;
			
			if(!merchant.equals("")){
				merchant=" ("+merchant+")";
			}else if(merchant.equals("kosong")){
				merchant="";
			}

			String text = number + "." + name+ " "+formatter.format(price)+"rb" + merchant;
			
			keyboard1 = new ArrayList<InlineKeyboardButton>();
			
			btn = new InlineKeyboardButton();
			btn.setText(text);
			btn.setCallbackData("nomor:" + number);
			keyboard1.add(btn);
			btn = new InlineKeyboardButton();
			
			btn.setText("-");
			btn.setCallbackData("minus:"+number);
			keyboard1.add(btn);
			
			
			keyboard.add(keyboard1);
			
		
		}
		
		inlineKeyboardMarkup.setKeyboard(keyboard);

		sendMessage.setReplayMarkup(inlineKeyboardMarkup);

		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void getMenuOrderInlineReply(Message message,Integer userId, Long chatId, Integer messageId, String caption, String search)
			throws TelegramApiException {
		
		System.out.println("masuk reply");
		EditMessageReplyMarkup sendMessage = new EditMessageReplyMarkup();
		
		sendMessage.setChatId(chatId.toString());
		//sendMessage.enableHtml(true);
		sendMessage.setMessageId(message.getMessageId());

		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

		List<List<InlineKeyboardButton>> keyboard = new ArrayList<List<InlineKeyboardButton>>();
		List<InlineKeyboardButton> keyboard1 = new ArrayList<InlineKeyboardButton>();
		
		menu = DatabaseTsel.getInstance().getMenu();
		
		NumberFormat formatter = new DecimalFormat("#0.##");
		
		InlineKeyboardButton btn = new InlineKeyboardButton();
		for (int i = 0; i < menu.size(); i++) {
			String number = menu.get(i).get("number");
			String merchant=menu.get(i).get("merchant");
			String name=menu.get(i).get("name");
			double price=Double.parseDouble(menu.get(i).get("price"));
			price=(double)price/1000;
			
			if(!merchant.equals("")){
				merchant=" ("+merchant+")";
			}else if(merchant.equals("kosong")){
				merchant="";
			}

			//String text = number + "." + name+ " "+formatter.format(price)+"rb" + merchant;
			String text="testing";
			
			btn = new InlineKeyboardButton();
			btn.setText(text);
			
			btn.setCallbackData("nomor:" + number);
			//keyboard1 = new ArrayList<InlineKeyboardButton>();
			keyboard1 = new ArrayList<InlineKeyboardButton>();
			keyboard1.add(btn);
			
			keyboard.add(keyboard1);
			
		
		}
		
		inlineKeyboardMarkup.setKeyboard(keyboard);

		//sendMessage.setInlineMessageId(inlineMessageId)
		/*
		 * ReplyKeyboardHide replyKeyboardHide = new ReplyKeyboardHide();
		 * replyKeyboardHide.setSelective(true);
		 */
		//sendMessage.setReplayMarkup(inlineKeyboardMarkup);
		sendMessage.setReplyMarkup(inlineKeyboardMarkup);

		/*
		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}
	


	private String getUsername(Message message) {

		String username = message.getFrom().getUserName();
		if (null==username || username.equals("null"))
			username = message.getFrom().getFirstName();
		if (null==username || username.equals("null"))
				username = message.getContact().getPhoneNumber();
		if (null==username || username.equals("null"))
				username="";
		
		return username;

	}

	// region Incoming messages handlers
	private void handleIncomingMessage(Message message) throws TelegramApiException {
		//////////////////////
		// cek register
		String username = null;
		String phone = null;
		String location = null;
		String language = null;

		int userId = message.getFrom().getId();
		username = getUsername(message);

		Long chatId = message.getChatId();
		Integer messageId = message.getMessageId();
		String caption = "";
		BotLogger.debug(LOGTAG, userId + "> " + message.getText());

		String msg = message.getText().toLowerCase();

		String[] member = DatabaseTsel.getInstance().getMember(message.getFrom().getId());
		BotLogger.debug(LOGTAG, "registered:" + member.length);
		

		NumberFormat formatter = new DecimalFormat("#0.##");
		

		if (member.length > 0) {
			phone = member[1];
			location = member[2];
			language = member[3];

			if (phone != null) {
				registered = true;
			}

		}
		//cek agen group
		boolean agenGroup=false;
		if (message.getChat().isGroupChat() && 
				message.getChat().getId().equals(BotConfig.GROUP_AGENKAFETARIA)){
			agenGroup=true;
			
			//group command
			if(msg.startsWith("/")){
				caption = "Maaf, group ini hanya untuk penggunaan Agen dan Merchant.";
				caption += "\nOrder silakan klik @kafetariabot";
				
				sendReplyMessageHTML(message,userId, chatId, messageId, caption);
			}
		}
		
		if(!registered) {

			if (message.getChat().isGroupChat()) {
				caption = "Kamu belum terdaftar, silakan klik @kafetariabot dan ketik <b>order</b> untuk registrasi";
				sendReplyMessageHTML(message,userId, chatId, messageId, caption);

			} else {
				sendRegisterMessage(message, language);
			}

		} else if(registered) {
			
			
			if(!agenGroup && (msg.startsWith("menu") 
					|| msg.startsWith("/menu")
					|| msg.startsWith("start")
					|| msg.startsWith("/start"))) {

				userState = new HashMap<String, String>();
				caption = "Halo " + username + " !! Silakan pilih menu dibawah ";
				sendMainMenu(message, username, menu);

			}else if(!agenGroup && (msg.startsWith("stop") || msg.startsWith("/stop"))) {

				userState.clear();
				caption = "Terima kasih, bot dihentikan.\nKlik /start untuk mulai kembali";
				sendReplyMessageHTML(message,userId, chatId, messageId, caption);

			} else if(!agenGroup && (msg.startsWith(cari.toLowerCase())
					|| msg.startsWith("cari") 
					|| msg.startsWith("/cari"))) {
				
				if (userState == null)
					userState = new HashMap<String, String>();

				userState.put(userId + "_menu", "cari");
				userState.put(userId + "_step", "1");

				caption = "Silakan masukan kata kunci misal.. <b>nasi</b>";
				sendReplyMessageHTML(message,userId, chatId, messageId, caption);

			} else if(!agenGroup && (msg.startsWith(order.toLowerCase()) 
					|| msg.startsWith("order") 
					|| msg.startsWith("/order"))){
				
				
				if (userState == null)
					userState = new HashMap<String, String>();

				userState.put(userId + "_menu", "order");
				userState.put(userId + "_step", "1");

				caption = "Silakan order dengan format &lt;nomor_menu&gt;*&lt;jumlah&gt;,dst";
				caption += "\n" + "(cth: 1*1,3*1,5*1)";

				getMenuOrderInline(message,userId, chatId, messageId, caption,"");

			} else if(!agenGroup && (msg.startsWith(feedback.toLowerCase()))) {
				if (userState == null)
					userState = new HashMap<String, String>();

				userState.put(userId + "_menu", "feedback");
				userState.put(userId + "_step", "1");
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																
				caption = "\nSilakan ketik feedback kamu agar kita bisa improve lagi "
						+ Emoji.SMILING_FACE_WITH_OPEN_MOUTH;
				sendReplyMessageHTML(message,userId, chatId, messageId, caption);

			}else{
				
				if (userState == null)
					userState = new HashMap<String, String>();

				if (!agenGroup && !userState.isEmpty()) {

					if (userState.get(userId + "_menu").equals("order")) {

						// step1
						if (userState.get(userId + "_step").equals("1")) {
							try {
								String text = message.getText();
								String[] order_text = text.split(",");
								double order_price = 0;
								caption = "<b>Order Item:</b>";
								double tot=0;
								double _tot=0;
								int number=0;
		
								for (int x = 0; x < order_text.length; x++) {

									String[] _order = order_text[x].split("\\*");

									HashMap<String, String> order = DatabaseTsel.getInstance()
											.getMenuByNumber(Integer.parseInt(_order[0]));
									tot = Double.parseDouble(_order[1]) * Double.parseDouble(order.get("price"));
									
									String merchant=order.get("merchant");
									String _menu=order.get("name");
									if(!merchant.equals("")){
										merchant=" ("+merchant+")";
									}else if(merchant.equals("kosong")){
										merchant="";
									}
								
									number=x+1;
									_tot=(double)tot/1000;
									
									caption += "\n"+number+"." + order.get("name") + merchant+" (x" + _order[1] + ")=Rp" + formatter.format(_tot)+"rb";
									order_price = order_price + tot;

								}
								
								long fee = order_text.length * BotConfig.FEE_ITEM;
								double _fee=(double)fee/1000;
								
								order_price += fee;
								double _order_price=(double)order_price/1000;
								
								caption += "\n<b>Ongkir:</b>Rp" + formatter.format(_fee)+"rb";
								caption += "\n<b>Total:</b>Rp" + formatter.format(_order_price)+"rb";
								
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);
								userState.put(userId + "_total", order_price + "");
								userState.put(userId + "_fee", fee + "");
								userState.put(userId + "_item", caption);

								userState.put(userId + "_username", username);
								userState.put(userId + "_phone", phone);

								caption = "\nMau diantar kapan? dan kemana? "
										+ "\n(cth: skrg/bsk pagi, lt 16 break out)";
								caption += "\natau klik /stop untuk cancel order";
								
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

								userState.put(userId + "_step", "2");

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption += "\nMaaf, pastikan format benar";
								caption += "\natau klik /stop untuk cancel order";
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

							}
						}
						// step2
						else if (userState.get(userId + "_step").equals("2")) {
							try {

								userState.put(userId + "_destination", msg);
								userState.put(userId + "_step", "3");

								// send to group agent
								caption = "Ada tambahan informasi?";
								caption += "\n(cth: gak pedes)";
								caption += "\natau klik /stop untuk cancel order";
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption = "\nMaaf, pastikan format benar";
								caption += "\natau klik /stop untuk cancel order";
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

							}
						}
						// step2
						else if (userState.get(userId + "_step").equals("3")) {
							try {

								userState.put(userId + "_note", msg);
								userState.put(userId + "_step", "4");

								// send to group agent
								caption = "Mau bayar cash atau tcash?";
								caption += "\n(cth: cash, tcash)";
								caption += "\natau klik /stop untuk cancel order";
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption = "\nMaaf, pastikan format benar";
								caption += "\natau klik /stop untuk cancel order";
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

							}
						} else if (userState.get(userId + "_step").equals("4")) {
							try {

								userState.put(userId + "_payment", msg);
								userState.put(userId + "_step", "5");

								// send to group agent
								// caption="Order Item :";
								String item = userState.get(userId + "_item");
								String note = userState.get(userId + "_note");
								String destination = userState.get(userId + "_destination");
								String payment = userState.get(userId + "_payment");

								caption += item;
								caption += "\n<b>Note:</b>" + note;
								caption += "\n<b>Tujuan:</b>" + destination;
								caption += "\n<b>Payment:</b>" + payment;
								caption += "\n\nSilakan ketik <b>YA</b> untuk lanjut, atau klik /stop untuk cancel order";

								userState.put(userId + "_msgid", messageId + "");
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption = "\nMaaf, pastikan format benar";
								caption += "\natau klik /stop untuk cancel order";
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

							}
						} else if (userState.get(userId + "_step").equals("5")) {
							try {

								if (msg.indexOf("ya") != -1) {
									caption = "Terima kasih, mohon ditunggu sebentar.." + Emoji.HOURGLASS_WITH_SAND
											+ Emoji.HAPPY_PERSON_RAISING_ONE_HAND + Emoji.HAPPY_PERSON_RAISING_ONE_HAND;
									sendReplyMessageHTML(message,userId, chatId, messageId, caption);

								} else {
									caption = "Terima kasih, order telah dibatalkan";
									sendReplyMessageHTML(message,userId, chatId, messageId, caption);

								}

								String item = userState.get(userId + "_item");
								String note = userState.get(userId + "_note");
								String destination = userState.get(userId + "_destination");
								String _username = userState.get(userId + "_username");
								String _phone = userState.get(userId + "_phone");
								String fee = userState.get(userId + "_fee");
								String total = userState.get(userId + "_total");
								String payment = userState.get(userId + "_payment");

								// sample
								caption = "<b>From:</b>@" + _username + " (" + _phone + ")";
								caption += "\n" + item;
								caption += "\n<b>Note:</b>" + note;
								caption += "\n<b>Tujuan:</b>" + destination;
								caption += "\n<b>Payment:</b>" + payment;

								// insert to database
								int orderId = DatabaseTsel.getInstance().addOrder(userId, _username, _phone, 0, "", "",
										chatId, messageId, item, Double.parseDouble(total), Double.parseDouble(fee), payment,
										0);
								if (orderId > 0) {
									sendMessageOrderToGroup(userId, BotConfig.GROUP_AGENKAFETARIA, caption, orderId);
									// clear state
									userState.clear();
								}
							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption = "\nMaaf, pastikan format benar";
								caption += "\natau klik /stop untuk cancel order";
								sendReplyMessageHTML(message,userId, chatId, messageId, caption);

							}
						} else {
							caption = "\nMaaf, pastikan format benar";
							caption += "\natau klik /stop untuk cancel order";
							sendReplyMessageHTML(message,userId, chatId, messageId, caption);
							userState.clear();
						}

					} else if (userState.get(userId + "_menu").equals("cari")) {
						if (userState.get(userId + "_step").equals("1")) {
							
							
							String search=msg;
							userState.put(userId + "_menu", "order");
							userState.put(userId + "_step", "1");

							caption = "Silakan order dengan format &lt;nomor_menu&gt;*&lt;jumlah&gt;,dst";
							caption += "\n" + "(cth: 1*1,3*1,5*1)";

							getMenuOrderInline(message,userId, chatId, messageId, caption, search);

							
						}
					} else if (userState.get(userId + "_menu").equals("feedback")) {
						if (userState.get(userId + "_step").equals("1")) {

							
							// insert to database
							int feedback = DatabaseTsel.getInstance().addFeedBack(userId, username, phone, msg);
							caption = "\nTengkyoo, feedback dari kamu sudah kita terima " + Emoji.THUMBS_UP_SIGN;
							sendReplyMessageHTML(message,userId, chatId, messageId, caption);
							userState.clear();
						}
					} // end menu order/feedback

				} // end userStateEmpty
			} // end else menu
		} // end is register
	///////////////////////////////////////////////////
	}



	private void sendMainMenu(Message message, String userName, ArrayList<HashMap<String, String>> menu)
			throws TelegramApiException {
		
		SendMessage sendMessage = new SendMessage();
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboad(true);

		List<KeyboardRow> keyboard = new ArrayList<>();
		int i = 0;
		KeyboardRow row = null;
		// int total=(int) Math.ceil(menu.size()/3);

		int mod = 2;
		for (i = 0; i < menu.size(); i++) {
			if (i % mod == 0) {
				row = new KeyboardRow();
				row.add(menu.get(i).get("menu"));
			} else {
				row.add(menu.get(i).get("menu"));

			}
			/*if ((i + 1) % mod == 0) {
				keyboard.add(row);
			}*/
			
			 if(i==(menu.size()-1)){ keyboard.add(row); }
			
		}

		replyKeyboardMarkup.setKeyboard(keyboard);

		String reply = "";
		Calendar c = Calendar.getInstance();
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
		if (timeOfDay >= 0 && timeOfDay < 12) {
			reply = "Halo bro *" + userName + "* !!";
		} else if (timeOfDay >= 12 && timeOfDay < 16) {
			reply = "Hai bro *" + userName + "* !!";
		} else if (timeOfDay >= 16 && timeOfDay < 18) {
			reply = "Apakabar bro *" + userName + "* !!";
		} else if (timeOfDay >= 18 && timeOfDay < 24) {
			reply = "Hola bro *" + userName + "* !!";
		}

		reply += "\nSilakan pilih menu dibawah : ";

		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(message.getChatId().toString());
		sendMessage.setReplayToMessageId(message.getMessageId());
		sendMessage.setReplayMarkup(replyKeyboardMarkup);
		sendMessage.setText(reply);

		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendMessageOrderToGroup(Integer userId, Long chatId, String text, int orderId)
			throws TelegramApiException {

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId.toString());
		sendMessage.enableHtml(true);
		String newtext = "<b>Order Id:</b>" + orderId;
		newtext += "\n" + text;
		sendMessage.setText(newtext);

		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

		List<List<InlineKeyboardButton>> keyboard = new ArrayList<List<InlineKeyboardButton>>();
		List<InlineKeyboardButton> keyboard1 = new ArrayList<InlineKeyboardButton>();

		InlineKeyboardButton btn = new InlineKeyboardButton();
		btn.setText("Ambil Order");
		btn.setCallbackData("ambilorder:" + orderId);
		BotLogger.debug(LOGTAG, "**************");
		keyboard1.add(btn);

		keyboard.add(keyboard1);
		inlineKeyboardMarkup.setKeyboard(keyboard);

		/*
		 * ReplyKeyboardHide replyKeyboardHide = new ReplyKeyboardHide();
		 * replyKeyboardHide.setSelective(true);
		 */
		sendMessage.setReplayMarkup(inlineKeyboardMarkup);

		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendPhotoMenu(Integer userId, Long chatId, Integer messageId, String caption)
			throws TelegramApiException {

		SendPhoto sendPhoto = new SendPhoto();
		sendPhoto.setChatId(chatId.toString());

		// sendPhoto.setPhoto("data_"+userId);
		String filepath = BuildVars.pathToData + "menu.png";
		sendPhoto.setNewPhoto(new File(filepath));
		sendPhoto.setCaption(caption);

		try {
			sendPhoto(sendPhoto);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	private void sendRegisterMessage(Message message, String language) {

		SendMessage sendMessage = new SendMessage();
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboad(true);

		List<KeyboardRow> keyboard = new ArrayList<>();
		KeyboardButton register = new KeyboardButton();
		register.setText("Registrasi Nomor");
		register.setRequestContact(true);

		KeyboardRow keyboardFirstRow = new KeyboardRow();
		keyboardFirstRow.add(register);
		keyboard.add(keyboardFirstRow);
		replyKeyboardMarkup.setKeyboard(keyboard);

		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(message.getChatId().toString());
		sendMessage.setReplayToMessageId(message.getMessageId());
		sendMessage.setReplayMarkup(replyKeyboardMarkup);
		sendMessage.setText("Halo " + message.getFrom().getFirstName()
				+ ",\nSilakan klik registrasi nomor kontak untuk pertama kali.");

		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	private void handleIncomingCallbackQuery(CallbackQuery callbackQuery) {
		String query = callbackQuery.getData();

		BotLogger.debug(LOGTAG, "Searching: " + query);
		BotLogger.debug(LOGTAG, "get:" + callbackQuery.getData());
		try {
			answerCallbackQuery(sendAnswerMessage(callbackQuery));
			
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private AnswerCallbackQuery sendAnswerMessage(CallbackQuery callbackQuery) {
		AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
		answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
		// answerCallbackQuery.setShowAlert(true);

		Message message=callbackQuery.getMessage();
		String command = callbackQuery.getData();
		BotLogger.debug(LOGTAG, "callback:" + command);

		if (command.startsWith("nomor:")) {
			String title="Silakan pilih nomor:";
			
				System.out.println("-=-----------------");
				//System.out.println(menulist.size());
				//sendMenuKeyboard(message, getUsername(message), title, menulist);
				//getMenuOrderInlineReply(message,message.getFrom().getId(),message.getChatId(),message.getMessageId(),"Next","bubur");
				
				EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
				//String inlineMessageId = callbackQuery.getInlineMessageId();
				System.out.println(message.getChatId()+":"+message.getMessageId());
				editMessageReplyMarkup.setChatId(message.getChatId()+"");
				editMessageReplyMarkup.setMessageId(message.getMessageId());
				//sendMessage.setInlineMessageId(inlineMessageId);
				
				InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

				List<List<InlineKeyboardButton>> keyboard = new ArrayList<List<InlineKeyboardButton>>();
				List<InlineKeyboardButton> keyboard1 = new ArrayList<InlineKeyboardButton>();

				InlineKeyboardButton btn = new InlineKeyboardButton();
				btn.setText("Ambil Order");
				btn.setCallbackData("ssss");
				BotLogger.debug(LOGTAG, "**************");
				keyboard1.add(btn);

				keyboard.add(keyboard1);
				inlineKeyboardMarkup.setKeyboard(keyboard);

				/*
				 * ReplyKeyboardHide replyKeyboardHide = new ReplyKeyboardHide();
				 * replyKeyboardHide.setSelective(true);
				 */
				editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
				
				try {
					editMessageReplyMarkup(editMessageReplyMarkup);
				} catch (TelegramApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
			
			
		}
		if (command.startsWith("ambilorder")) {

			int orderId = Integer.parseInt(command.split(":")[1]);
			int userId = callbackQuery.getFrom().getId();

			String[] member = DatabaseTsel.getInstance().getMember(userId);
			BotLogger.debug(LOGTAG, "registered:" + member.length);
			String phone = "62811xxxxxxx";
			String username = "";

			if (member.length > 0) {
				username = member[0];
				phone = member[1];
			}

			String resp = "";
			///////////////////////////////////////////
			// Send Message to group
			////////////////////////////////////////////
			// check first
			HashMap<String, String> orderItem = DatabaseTsel.getInstance().getOrder(orderId);
			boolean infoCustomer = false;

			if (orderItem.get("agent_userid").equals("0")) {
				boolean res = DatabaseTsel.getInstance().updateOrder(orderId, userId, username, phone, 1);
				if (res) {
					resp = "Order diambil oleh:";
					resp += "\n@" + callbackQuery.getFrom().getUserName();
					resp += " (" + phone + ")";

					infoCustomer = true;

				} else {
					resp = "Maaf, terjadi kesalahan pada system";
				}
				// answerCallbackQuery.setText(resp);
			} else {
				resp = "Maaf, order sudah diambil oleh:";
				resp += "\n@" + orderItem.get("agent_username") + " (" + orderItem.get("agent_userphone") + ")";

			}
			SendMessage sendMessage = new SendMessage();
			sendMessage.enableMarkdown(true);
			sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
			sendMessage.setReplayToMessageId(callbackQuery.getMessage().getMessageId());

			sendMessage.setText(resp);

			try {
				sendMessage(sendMessage);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// info to customer
			if (infoCustomer) {
				/*NumberFormat formatter = new DecimalFormat("#0.##");
				double total=Double.parseDouble(orderItem.get("total"))/1000;
				*/
				String item=orderItem.get("item");
				
				resp = "Selamat order kamu segera diproses !!";
				resp += "\n<b>Order Id:</b>" + orderId;
				resp += "\n"+item;
				resp += "\n<b>Diproses Oleh:</b>@" + username + " (" + phone + ")";

				String payment = orderItem.get("payment");
				if (payment.indexOf("tcash") != -1) {
					resp += "\nSilakan melakukan pembayaran tcash ke nomor diatas";
				}

				sendMessage = new SendMessage();
				sendMessage.enableHtml(true);
				sendMessage.setChatId(orderItem.get("chatid"));
				sendMessage.setReplayToMessageId(Integer.parseInt(orderItem.get("messageid")));
				sendMessage.setText(resp);
				

				try {
					sendMessage(sendMessage);
				} catch (TelegramApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} // end info customer

		}

		return answerCallbackQuery;
	}

	
	private void sendMenuKeyboard(Message message, String userName, String title, ArrayList<HashMap<String, String>> menulist)
			throws TelegramApiException {
		
		SendMessage sendMessage = new SendMessage();
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboad(true);

		List<KeyboardRow> keyboard = new ArrayList<>();
		int i = 0;
		KeyboardRow row = null;
		
		int mod = 2;
		for (i = 0; i < menulist.size(); i++) {
			if (i % mod == 0) {
				row = new KeyboardRow();
				row.add(menulist.get(i).get("number"));
			} else {
				row.add(menulist.get(i).get("number"));

			}
			/*if ((i + 1) % mod == 0) {
				keyboard.add(row);
			}*/
			
			 if(i==(menu.size()-1)){ keyboard.add(row); }
			
		}

		replyKeyboardMarkup.setKeyboard(keyboard);

		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(message.getChatId().toString());
		sendMessage.setReplayToMessageId(message.getMessageId());
		sendMessage.setReplayMarkup(replyKeyboardMarkup);
		sendMessage.setText(title);

		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	

	public String getBotUsername() {
		// TODO Auto-generated method stub
		// return null;
		return BotConfig.NAME_SMARTOFFICE;
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		// return null;
		return BotConfig.TOKEN_SMARTOFFICE;
	}
}
