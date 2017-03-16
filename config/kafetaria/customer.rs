/*
	functionality that RiveScript 2 is supposed to support.
*/

/******************************************************************************
 * bismillah                                                *
 ******************************************************************************/

> begin
	+ request
	- {ok}
< begin

// Bot Variables
! var name     = Nugie
! var umur     = 1
! var kelamin  = cowo
! var lokasi   = jakarta selatan
! var telepon  = 62811800
! var email    = test@telkomsel.co.id

// Substitutions
! sub +         = plus
! sub -         = minus
! sub /         = divided
! sub *         = times
! sub aku       = saya
! sub gw       	= saya
! sub gue      	= saya
! sub lo      	= kamu
! sub elo     	= kamu

// Person substitutions
! person saya    = kamu
! person kamu 	 = saya

// Arrays
! array warna = merah hijau biru kuning putih orange coklat hitam
  ^ red green blue cyan yellow magenta white orange brown black
  ^ gray grey fuchsia maroon burgundy lime navy aqua gold silver copper bronze
  ^ light red|light green|light blue|light cyan|light yellow|light magenta


+ menu
- menu

+ *
@ menu


/******************************************************************************
 * Topic Order	                                                             *
 ******************************************************************************/
+ [*] order [*]
- {topic=order}{@help}

> topic order
	+ help
	- order
	
	+ order [*]
	- pick 
	
	+ [*] lantai|lt|break|out|meja [*] 
	- note
	
	+ (note|no) [*]
	- conf
	
	+ [*] (ya|yes|ok|iya) [*]
	- drop
	
	
	
	+ *
	- Maap, bisa diulang lagi . atau ketik batal
	- sorry, gak konsen nih. bisa ketik ulang. atau ketik 'batal' 
		
	+ [*] batal|menu [*]
	- {topic=random} {@menu}
< topic



/******************************************************************************
 * Topic Transfer Pulsa	                                                       *
 ******************************************************************************/
+ [*] transfer pulsa [*]
- {topic=tp}{@help}

> topic tp
	+ help
	- kamu mau transfer pulsa ya? mau yang berapa (10 rb/20 rb/30 rb/50 rb/100 rb) ?
	
	+ [*] (10|20|30|50|100) [*]
	- <set quota=<star>>Sip, tunggu sebentar Nugie itung dulu harganya..{@xcall}
	- <set quota=<star>>Siap, sebentar ya tungguin Nugie mau cek harganya dulu ..{@xcall}
	- <set quota=<star>>Siap, sebentar ya tungguin Nugie mau cek harganya dulu ..{@xcall}
	
	+ xcall
	- <call>javatest tpulsa</call>
	
	
	+ *
	- sorry, lagi gak konsen nih. bisa ketik ulang. atau ketik menu kalau mau diulang
	- sorry ya bro, Nugie tadi dijalan gak konek nih, bisa ketik lagi..atau ketik 'batal'
		
	+ [*] batal|menu [*]
	- {topic=random} {@menu}
< topic


/******************************************************************************
 * Topic cek Pulsa	                                                       *
 ******************************************************************************/
+ [*] pulsa [*]
- {topic=pulsa}{@help}

> topic pulsa
	+ help
	- {@xcall}:Sip, tunggu sebentar Nugie cek dulu pulsa kamu..
	
	+ xcall
	- <call>javatest pulsa</call>
	
	
	
	+ *
	- sorry, lagi gak konsen nih. bisa ketik ulang. atau ketik menu kalau mau diulang
	- sorry ya, Nugie tadi dijalan gak konek nih, bisa ketik lagi..atau ketik 'batal'
		
	+ [*] batal|menu [*]
	- {topic=random} {@menu}
< topic





