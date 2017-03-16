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

public class MyKafetariaBot extends TelegramLongPollingBot {
	private static final String LOGTAG = "Kafetaria {}";
	
	ArrayList<HashMap<String, String>> menu = new ArrayList<HashMap<String, String>>();

	String existing_menu = Emoji.STATION + " Menu";
	String ganti_menu = Emoji.STATION + " Ganti Menu";
	
	String subscribe = Emoji.DELIVERY_TRUCK + " Langganan";
	String promo = Emoji.SPARKLES + " Promo";
	String feedback = Emoji.SPARKLES + " Feedback";

	HashMap<String, String> userState = null;

	public MyKafetariaBot() {
		
		HashMap<String, String> menu1 = new HashMap<String, String>();
		menu1.put("menu", ganti_menu);
		HashMap<String, String> menu2 = new HashMap<String, String>();
		menu2.put("menu", subscribe);
		HashMap<String, String> menu3 = new HashMap<String, String>();
		menu3.put("menu", promo);
		HashMap<String, String> menu4 = new HashMap<String, String>();
		menu4.put("menu", feedback);
		HashMap<String, String> menu5 = new HashMap<String, String>();

		HashMap<String, String> menu6 = new HashMap<String, String>();
		menu1.put("menu", existing_menu);
		
		menu.add(menu6);
		menu.add(menu1);
		// menu.add(menu2);
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
						// send main menu
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

	private void sendReplyMessage(Message message,Integer userId, Long chatId, Integer messageId, String text)
			throws TelegramApiException {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId.toString());
		sendMessage.enableMarkdown(true);
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

	private String getMenuHeader() {
		String resp = "Menu Hari Ini";
		String[] arrayEmoji = { "" + Emoji.BLACK_SUN_WITH_RAYS + Emoji.BLACK_SUN_WITH_RAYS + Emoji.BLACK_SUN_WITH_RAYS,
				"" + Emoji.BUS + Emoji.BICYCLE + Emoji.DELIVERY_TRUCK,
				"" + Emoji.CLOUD + Emoji.AUTOMOBILE + Emoji.FACE_SCREAMING_IN_FEAR,
				"" + Emoji.CLOSED_UMBRELLA + Emoji.CLOUD + Emoji.CONFOUNDED_FACE,
				"" + Emoji.CONSTRUCTION_SIGN + Emoji.DOOR + Emoji.EARTH_GLOBE_EUROPE_AFRICA };

		resp = arrayEmoji[0] + "<b>" + resp + "</b>" + arrayEmoji[0];
		return resp;

	}


	private void getMenuOrder(Message message,Integer userId, Long chatId, Integer messageId, String caption)
			throws TelegramApiException {
		
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId.toString());
		sendMessage.enableHtml(true);
		/*if(message.getChat().isGroupChat()){
			sendMessage.setReplayToMessageId(messageId);
		}*/
		
		ArrayList<HashMap<String, String>> menu = DatabaseTsel.getInstance().getMenu();
		String text = getMenuHeader() + "\n";
		
		NumberFormat formatter = new DecimalFormat("#0.##");
				
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

			text += "\n" + number + "." + name+ " "+formatter.format(price)+"rb" + merchant;
			
		}
		text += "\n\n" + caption;
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

	private void getMenuOrderAdmin(Message message,Integer userId, Long chatId, Integer messageId, String caption, String phone)
			throws TelegramApiException {
		
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId.toString());
		sendMessage.enableHtml(true);
		if(message.getChat().isGroupChat()){
			sendMessage.setReplayToMessageId(messageId);
		}
		
		String username=message.getFrom().getUserName();
		ArrayList<HashMap<String, String>> menu = DatabaseTsel.getInstance().getMenuAdmin(username, userId, phone);
		String text = getMenuHeader() + "\n";
		
		NumberFormat formatter = new DecimalFormat("#0.##");
				
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

			text += "\n" + number + "." + name+ " "+formatter.format(price)+"rb" + merchant;
			
		}
		text += "\n\n" + caption;
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
		boolean adminGroup=false;
		if (message.getChat().isGroupChat() && 
				message.getChat().getId().equals(BotConfig.GROUP_ADMINKAFETARIA)){
			adminGroup=true;
		}
		
		boolean adminUser=false;
		if (BotConfig.USER_ADMINKAFETARIA.indexOf(message.getFrom().getUserName())!=-1){
			adminGroup=true;
			adminUser=true;
		}
		
		if(!registered) {

			if (message.getChat().isGroupChat()) {
				caption = "Kamu belum terdaftar, silakan klik @kafetariabot dan ketik *start* untuk registrasi";
				sendReplyMessage(message,userId, chatId, messageId, caption);

			} else {
				sendRegisterMessage(message, language);
			}

		} else if(registered) {
			
			if(adminUser && (msg.startsWith("setadmin"))){
				String[] x = msg.split(" ");
				//System.out.println(x[0]+"-"+x[1]+"-"+x[2]);
				int update=0;
				if(x[2].equals("username")){
					update=DatabaseTsel.getInstance().setMenuAdmin(Integer.parseInt(x[1]), 0, x[3],"");
				}else if(x[2].equals("phone")){
					update=DatabaseTsel.getInstance().setMenuAdmin(Integer.parseInt(x[1]), 0, "", x[3]);
				}
				
				//if(update>0)
					caption = "Terima kasih, admin untuk merchant ID "+x[1]+" sudah ditambahkan untuk "+ x[3];
				//else
					//caption = "Maaf, pastikan format sudah benar";
				
				sendReplyMessage(message,userId, chatId, messageId, caption);

			}
			/*if(adminUser && (msg.startsWith("addmenu"))){
				String[] x = msg.split(" ");
				if(x[2].equals("username")){
					DatabaseTsel.getInstance().setMenuAdmin(Integer.parseInt(x[1]), 0, x[3],"");
				}
				if(x[2].equals("phone")){
					DatabaseTsel.getInstance().setMenuAdmin(Integer.parseInt(x[1]), 0, "", x[3]);
				}
				
				caption = "Terima kasih, admin untuk merchant ID "+x[1]+" sudah ditambahkan untuk "+ x[3];
				sendReplyMessage(message,userId, chatId, messageId, caption);

			}
			*/

			
			if (userState == null)
				userState = new HashMap<String, String>();
			
			if(adminGroup &&  (msg.startsWith("start")
					|| msg.startsWith("/start"))) {

				userState = new HashMap<String, String>();
				caption = "Halo " + username + " !! Silakan pilih menu dibawah ";
				sendMainMenu(message, username, menu);

			}else if(adminGroup && (msg.startsWith("stop") 
					|| msg.startsWith("/stop"))) {

				userState.clear();
				caption = "Terima kasih, bot berhasil dihentikan.\nKlik /start untuk mulai kembali";
				sendReplyMessage(message,userId, chatId, messageId, caption);

			} else if(adminGroup && (msg.equals(existing_menu.toLowerCase()) || msg.startsWith("menu") 
					|| msg.startsWith("/menu"))) {
				
				caption = "Silakan klik /ganti_harga atau /ganti_menu";
				getMenuOrder(message, userId, chatId, messageId, caption);

			} else if(adminGroup && (msg.startsWith(feedback.toLowerCase()))) {
				if (userState == null)
					userState = new HashMap<String, String>();

				userState.put(userId + "_menu", "feedback");
				userState.put(userId + "_step", "1");

				caption = "\nSilakan ketik feedback kamu agar kita bisa improve lagi "
						+ Emoji.SMILING_FACE_WITH_OPEN_MOUTH;
				sendReplyMessage(message,userId, chatId, messageId, caption);

			}else if(adminGroup && (msg.equals("ganti"))) {
				caption = "Silakan klik /ganti_harga atau /ganti_menu";
				sendReplyMessage(message,userId, chatId, messageId, caption);
				
			}else if(adminGroup && (msg.startsWith("ganti menu") 
					|| msg.startsWith("/ganti_menu"))) {

				userState.put(userId + "_menu", "ganti_menu");
				userState.put(userId + "_step", "1");
				
				caption = "Silakan pilih nomor menu yg mau diganti ?";
				getMenuOrderAdmin(message, userId, chatId, messageId, caption, phone);
				
			}else if(adminGroup && (msg.startsWith("ganti harga") 
					|| msg.startsWith("/ganti_harga"))) {

				userState.put(userId + "_menu", "ganti_harga");
				userState.put(userId + "_step", "1");
				
				caption = "Silakan pilih nomor menu yg mau diganti ?";
				getMenuOrderAdmin(message, userId, chatId, messageId, caption, phone);
				
				
			}else{
				
				if (userState == null)
					userState = new HashMap<String, String>();

				if (adminGroup && !userState.isEmpty()) {

					if (userState.get(userId + "_menu").equals("ganti_menu")) {

						// step1
						if (userState.get(userId + "_step").equals("1")) {
							try {
								
								userState.put(userId + "_step", "2");
								userState.put(userId + "_menu_number", msg);
								
								boolean ok = DatabaseTsel.getInstance().getMenuPrevileges(Integer.parseInt(msg), username, userId, phone);
								
								if(ok){
									caption = "\nSilakan ketik nama menu ?";
								}else{
									caption = "\nSorry, previleges kamu tidak bisa edit nomor menu ini " + Emoji.FACE_SCREAMING_IN_FEAR;
								}
								
								sendReplyMessage(message,userId, chatId, messageId, caption);
								

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption += "\nMaaf, pastikan format yang dikirim benar";
								caption += "\natau ketik batal untuk cancel order";
								sendReplyMessage(message,userId, chatId, messageId, caption);

							}
						}else if (userState.get(userId + "_step").equals("2")) {
							
							try {
								
								userState.put(userId + "_step", "3");
								userState.put(userId + "_menu_name", message.getText());
								caption = "\nSilakan ketik nama merchant ?";
								
								sendReplyMessage(message,userId, chatId, messageId, caption);
								

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption += "\nMaaf, pastikan format yang dikirim benar";
								caption += "\natau ketik batal untuk cancel order";
								sendReplyMessage(message,userId, chatId, messageId, caption);

							}
						
						}else if (userState.get(userId + "_step").equals("3")) {
							
							try {
								
								userState.put(userId + "_step", "4");
								userState.put(userId + "_menu_merchant", message.getText());
								caption = "\nSilakan ketik nominal harga ?";
								
								sendReplyMessage(message,userId, chatId, messageId, caption);
								

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption += "\nMaaf, pastikan format yang dikirim benar";
								caption += "\natau ketik batal untuk cancel order";
								sendReplyMessage(message,userId, chatId, messageId, caption);

							}
						}else if (userState.get(userId + "_step").equals("4")) {
							
							try {
								
								userState.put(userId + "_step", "5");
								userState.put(userId + "_menu_price", msg);
								String number=userState.get(userId + "_menu_number");
								String name=userState.get(userId + "_menu_name");
								String merchant=userState.get(userId + "_menu_merchant");
								String price=userState.get(userId + "_menu_price");
							
								

								if(!merchant.equals("")) merchant=" ("+merchant+")";
								double _price=(double)Double.parseDouble(price)/1000;
								
								
								caption = "Menu yang akan di dirubah sbb :";
								caption += "\n"+number+"."+name+" "+formatter.format(_price)+"rb"+merchant;
								caption += "\n\nKetik ya untuk lanjut, atau kilk /stop untuk cancel perubahan";
								
								sendReplyMessage(message,userId, chatId, messageId, caption);
								

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption += "\nMaaf, pastikan format yang dikirim benar";
								caption += "\natau klik /stop untuk cancel order";
								
								sendReplyMessage(message,userId, chatId, messageId, caption);

							}
						}else if (userState.get(userId + "_step").equals("5")) {
							
							try {
								
								//userState.put(userId + "_step", "6");
								String number=userState.get(userId + "_menu_number");
								String name=userState.get(userId + "_menu_name");
								String merchant=userState.get(userId + "_menu_merchant");
								String price=userState.get(userId + "_menu_price");
							
								double _price=(double)Double.parseDouble(price)/1000;
								
								boolean ok = DatabaseTsel.getInstance().updateMenu(Integer.parseInt(number), name, merchant, Integer.parseInt(price));
								
								if(ok){
									caption = "Selamat, menu sukses di rubah. klik /menu untuk melihat menu terakhir.";
								}else{
									caption = "Sorry, ada yang error pada saat update menu " + Emoji.FACE_SCREAMING_IN_FEAR;
									caption += "\nSilakan coba lagi";
								}
								
								sendReplyMessage(message,userId, chatId, messageId, caption);
								userState.clear();
							

							} catch (Exception e) {
								// TODO: handle exception
								System.out.println(e.getMessage());
								caption += "\nMaaf, pastikan format yang dikirim benar";
								caption += "\natau klik /stop untuk cancel order";
								sendReplyMessage(message,userId, chatId, messageId, caption);

							}
						}
						
					/////////
					}else if (userState.get(userId + "_menu").equals("ganti_harga")) {

							if (userState.get(userId + "_step").equals("1")) {
								
								try {
									
									userState.put(userId + "_step", "2");
									userState.put(userId + "_menu_number", msg);
									
									boolean ok = DatabaseTsel.getInstance().getMenuPrevileges(Integer.parseInt(msg), username, userId, phone);
									
									if(ok){
										caption = "\nSilakan ketik nominal harga ?";
									}else{
										caption = "\nSorry, previleges kamu tidak bisa edit nomor menu ini " + Emoji.FACE_SCREAMING_IN_FEAR;
									}
									
									sendReplyMessage(message,userId, chatId, messageId, caption);
									

								} catch (Exception e) {
									// TODO: handle exception
									System.out.println(e.getMessage());
									caption += "\nMaaf, pastikan format yang dikirim benar";
									caption += "\natau klik /stop untuk cancel order";
									sendReplyMessage(message,userId, chatId, messageId, caption);

								}
							}else if (userState.get(userId + "_step").equals("2")) {
								
								try {
									
									
									userState.put(userId + "_step", "3");
									userState.put(userId + "_menu_price", msg);
								
									String price=msg;
									int number=Integer.parseInt(userState.get(userId+"_menu_number"));
									
									//get menu
									HashMap<String, String> menuOrder=DatabaseTsel.getInstance().getMenuByNumber(number);
									
									String name=menuOrder.get("name");
									String merchant=menuOrder.get("merchant");
									
									//String price=menuOrder.get("price");
									//if(!merchant.equals("")) merchant=" ("+merchant+")";
									//double _price=(double)Double.parseDouble(price)/1000;
									
									
									/*caption = "Menu yang akan di dirubah sbb :";
									caption += "\n"+number+"."+name+" "+formatter.format(_price)+"rb"+merchant;
									caption += "\n\nKetik ya untuk lanjut, atau kilk /stop untuk cancel perubahan";
									*/								
									
									boolean ok = DatabaseTsel.getInstance().updateMenu(number, name, merchant, Integer.parseInt(price));
									
									if(ok){
										caption = "Selamat, harga sukses di rubah. klik /menu untuk melihat menu terakhir.";
									}else{
										caption = "Sorry, ada yang error pada saat update menu " + Emoji.FACE_SCREAMING_IN_FEAR;
										caption += "\nSilakan coba lagi";
									}
									
									sendReplyMessage(message,userId, chatId, messageId, caption);
									userState.clear();
									

								} catch (Exception e) {
									// TODO: handle exception
									System.out.println(e.getMessage());
									caption += "\nMaaf, pastikan format yang dikirim benar";
									caption += "\natau klik /stop untuk cancel order";
									sendReplyMessage(message,userId, chatId, messageId, caption);

								}
							
							}
							
						/////////
					}else if (userState.get(userId + "_menu").equals("feedback")) {
						if (userState.get(userId + "_step").equals("1")) {

							
							// insert to database
							int feedback = DatabaseTsel.getInstance().addFeedBack(userId, username, phone, msg);
							caption = "\nTengkyoo, feedback dari kamu sudah kita terima " + Emoji.THUMBS_UP_SIGN;
							sendReplyMessage(message,userId, chatId, messageId, caption);
							userState.clear();
						}
					} // end menu order/feedback

				} // end userStateEmpty
				if(!adminGroup){
					caption = "\nSorry, akses hanya dari group khusus " + Emoji.FACE_SCREAMING_IN_FEAR;
					sendReplyMessage(message,userId, chatId, messageId, caption);
			
				}
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
			if ((i + 1) % mod == 0) {
				keyboard.add(row);
			}
			/*
			 * if(i==(menu.size()-1)){ keyboard.add(row); }
			 */

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

		String command = callbackQuery.getData();
		BotLogger.debug(LOGTAG, "callback:" + command);

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
				NumberFormat formatter = new DecimalFormat("#0.##");
				

				resp = "Selamat order kamu segera diproses !!";
				resp += "\n<b>Order Id:</b>" + orderId;
				resp += "\n<b>Total :</b>Rp." + formatter.format(orderItem.get("total"))+"rb";
				resp += "\n<b>Diproses Oleh :</b>@" + username + " (" + phone + ")";

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

	public String getBotUsername() {
		// TODO Auto-generated method stub
		// return null;
		return BotConfig.NAME_MYKAFETARIA;
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		// return null;
		return BotConfig.TOKEN_MYKAFETARIA;
	}
}
