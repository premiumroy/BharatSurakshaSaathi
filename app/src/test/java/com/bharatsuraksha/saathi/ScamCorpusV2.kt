package com.bharatsuraksha.saathi

/** What the corpus expects the engine to say. */
enum class Expect {
    /** Genuine message — must come out SAFE (no clear warning signs). */
    SAFE,
    /** A scam — must NOT be SAFE. */
    WARN,
    /** A clear scam — must be DANGEROUS. */
    DANGER
}

data class CorpusItem(val text: String, val expect: Expect, val family: String = "")

/**
 * v2 regression corpus: 130+ labelled messages — scams across every family
 * (including evasion tricks) plus genuine bank / OTP / bill / delivery / family
 * messages in English, Hindi and Hinglish.
 *
 * Ground truth lives here; the engine is judged against it by ScamCorpusV2Test.
 */
object ScamCorpusV2 {

    private fun g(t: String) = CorpusItem(t, Expect.SAFE)
    private fun w(t: String, f: String = "") = CorpusItem(t, Expect.WARN, f)
    private fun d(t: String, f: String = "") = CorpusItem(t, Expect.DANGER, f)

    val all: List<CorpusItem> = listOf(

        // ===== Phase 6 required messages: MUST be Careful or Dangerous =====
        w("Hi, मैं राहुल बोल रहा हूँ, आपके पुराने कॉलेज से। नंबर बदल गया है, सेव कर लेना। एक जरूरी हेल्प चाहिए थी, फ्री हो तो बताना?", "family_emergency"),
        w("आपके अकाउंट में गलती से ₹20,000 आ गए हैं, कृपया वापस भेज दें", "refund_tax_bill"),
        w("मम्मी, मेरा फोन टूट गया, ये नया नंबर है। अभी UPI से ₹6,000 भेजो, किसी को मत बताना।", "family_emergency"),
        w("Task complete! आपकी कमाई ₹850 वॉलेट में जुड़ गई। अगला टास्क अनलॉक करने के लिए ₹2,000 जमा करें।", "task_job"),
        w("आपके नाम से एक अंतरराष्ट्रीय पार्सल कस्टम में पकड़ा गया है। जाँच के लिए तुरंत संपर्क करें।", "courier_delivery"),
        w("आपको Smart Traders VIP ग्रुप में जोड़ा गया है। 3 दिन में 40% मुनाफा।", "investment"),

        // ===== Phase 6 required messages: MUST stay "No clear warning signs" =====
        g("OTP 391827 for transaction of Rs 1,500 at Amazon. Do not share this OTP with anyone."),
        g("Rs 520.00 debited from A/c XX1234 at Big Bazaar. Avl bal Rs 12,340."),
        g("आपके ₹2,000 के चेक का भुगतान बाउंस हो गया है। कृपया खाते में पर्याप्त राशि रखें।"),
        g("पापा, मैं कॉलेज से निकल गया हूँ, शाम तक घर पहुँचूँगा।"),
        g("भाई शाम को फ्री हो तो चलते हैं।"),
        g("आपकी LIC पॉलिसी का प्रीमियम अगले हफ्ते देय है। रसीद के लिए शाखा से संपर्क करें।"),

        // ================= GENUINE (must stay SAFE) — 62 =================
        g("Hi, are we still meeting for lunch at 1pm?"),
        g("Your order #A1234 has been shipped and will reach you tomorrow."),
        g("Mom, I'll be late today, please keep dinner aside."),
        g("Dear customer, your account statement for January is ready in the app."),
        g("Reminder: dentist appointment tomorrow at 10am."),
        g("Team, the meeting is moved to 4pm. Please update your calendars."),
        g("Congratulations to Rohan for winning the office quiz!"),
        g("Your OTP for login is 4821. Do not share it with anyone."),
        g("Airtel: Your data pack expires on 30 Jan. Recharge to continue."),
        g("Your package is out for delivery today."),
        g("Dad, I reached college safely."),
        g("Please find attached the invoice for December."),
        g("Your appointment with Dr. Sharma is confirmed for Monday."),
        g("Happy Diwali! Wishing you and your family joy and prosperity."),
        g("Mobile bill of Rs 499 is due on 15 Jan. Pay via the official app."),
        g("Aapka OTP kisi ko na batayein. Bank kabhi OTP nahi maangta."),
        g("Rs 2500 debited from A/c XX1234 on 12-01. Not you? Call 18001234."),
        g("Your a/c has been credited with Rs 15000. Available balance Rs 32000."),
        g("HDFC Bank: Your card ending 4321 was used for Rs 899. If not you, call the number on your card."),
        g("SBI: Rs 500 debited for SIP. Ignore if already done."),
        g("Dear customer, your electricity bill of Rs 1200 is generated. Pay before 25 Jan via the app."),
        g("Your water bill payment was successful. Thank you."),
        g("Your Swiggy order has been delivered. Enjoy your meal!"),
        g("Amazon: Your order has been delivered. Rate your experience."),
        g("Flipkart: Your order is packed and will be dispatched soon."),
        g("India Post: Your consignment has reached your local post office."),
        g("Your train PNR 4567890 is confirmed. Happy journey."),
        g("IRCTC: Your ticket has been booked. Check the app for details."),
        g("Reminder: submit your timesheet by Friday."),
        g("Team lunch today at 1:30pm. Join us if free."),
        g("Beta, khana kha liya? Ghar kab aa rahe ho?"),
        g("Mummy, main thik hoon. Raat ko aa jaunga."),
        g("Kal subah 8 baje milte hain. Theek hai?"),
        g("Free ho? Ek baar call kar lo."),
        g("Your policy premium of Rs 5000 is due on 20 Jan. Pay via the insurer's app."),
        g("Your internet plan will renew on 1 Feb. No action needed."),
        g("Dear user, your gas booking is confirmed. Delivery in 2 days."),
        g("Your UPI payment of Rs 200 to Ramesh was successful."),
        g("Payment received: Rs 1500 from Suresh. Thank you."),
        g("School: Parent-teacher meeting on Saturday at 10am."),
        g("Your blood test report is ready. Please collect it from the lab."),
        g("Congratulations on your promotion! Well deserved."),
        g("Your leave request has been approved."),
        g("The cab will arrive in 3 minutes. OTP for the ride is 4412."),
        g("Your DTH recharge of Rs 300 was successful."),
        g("Your new debit card has been dispatched and will reach you in 5 working days."),
        g("Bank: Your KYC verification is complete. Thank you."),
        g("UIDAI: Your Aadhaar address update is complete."),
        g("Your mobile number has been successfully linked to your bank account."),
        g("Please collect your parcel from the society gate."),
        g("Your parcel is delayed due to rain. Sorry for the inconvenience."),
        g("Insurance: Your claim has been approved. Amount will be credited in 3 days."),
        g("Your refund of Rs 299 for the cancelled order has been processed to your original payment method."),
        g("Your order has been cancelled. Refund will be initiated in 3 days."),
        g("Dear customer, your SIP of Rs 1000 is due on 5 Feb."),
        g("Your credit card bill of Rs 5000 is due on 20 Jan. Pay via the app."),
        g("Your KYC is verified. You can now use all banking services."),
        g("Bijli ka bill bhar diya hai maine, aap chinta na karein."),
        g("Aaj shaam ko ghar aa raha hoon, khana bana lena."),
        g("Bhai kal ka match dekhna, 7 baje se."),
        g("Aapka order kal tak pahunch jayega, track kar lena."),

        // ================= SCAMS — no link / social engineering =================
        d("You are under digital arrest. Do not disconnect the video call. Transfer money to the government account for verification.", "digital_arrest"),
        d("CBI notice: a parcel with illegal items is in your name. You are under digital arrest. Call this number immediately.", "digital_arrest"),
        d("This is Inspector Sharma from CBI. A case is registered against you. Do not tell anyone and stay on the call.", "digital_arrest"),
        d("आप डिजिटल अरेस्ट में हैं। वीडियो कॉल मत काटें और किसी को न बताएं।", "digital_arrest"),
        d("पुलिस वेरिफिकेशन के लिए अपना पैसा सुरक्षित खाते में डालें, वरना गिरफ्तारी होगी।", "digital_arrest"),
        d("Your parcel has drugs. Customs case is registered. Pay 25000 to settle or police will arrest you.", "courier_delivery"),
        d("We found illegal items in a parcel sent to you from Dubai. Police case has been registered. Pay 25000 to settle.", "courier_delivery"),
        d("Aapke naam par parcel aaya hai jisme drugs hain. Police case ho jayega. Paise bhejo.", "courier_delivery"),
        d("Income Tax Department: A non-bailable warrant has been issued. Pay immediately or you will be arrested.", "digital_arrest"),
        d("Your account has been frozen by the police due to suspicious activity. Contact this number to avoid legal action.", "digital_arrest"),
        d("This is your son. This is my new number. I had an accident, please send money urgently. Don't tell mom.", "family_emergency"),
        d("Papa mera naya number hai. Hospital me admit hoon, jaldi paise bhej do, mummy ko mat batao.", "family_emergency"),
        d("Hi it's me, my phone is dead, this is a friend's number. I need money for an emergency. Please send 20000.", "family_emergency"),
        d("Guaranteed returns! Invest 10000 and get 20000 in 30 days. Join our telegram group now.", "investment"),
        d("Join our VIP trading group. Daily 5% profit guaranteed. Limited seats.", "investment"),
        d("Invest in our app. It shows profit but to withdraw you must pay 20% tax first.", "investment"),
        d("आपका पैसा 15 दिन में दोगुना। गारंटीड रिटर्न। टेलीग्राम ग्रुप जॉइन करें।", "investment"),
        d("Work from home! Earn Rs 5000 per day. Registration fee Rs 500 only. Pay now and start today.", "task_job"),
        d("Complete simple tasks and earn. Unlock the next task by depositing 1000 in your wallet.", "task_job"),
        d("Part time job: like and subscribe videos, earn per task. Pay 500 joining fee on WhatsApp.", "task_job"),
        d("घर बैठे काम करें, रोज़ 3000 कमाएँ। पहले रजिस्ट्रेशन फीस 500 भेजें।", "task_job"),
        d("You have won Rs 25 lakh in the KBC lucky draw. Click here to claim your prize: bit.ly/xyz", "lottery_prize"),
        d("Congratulations! You are the lucky winner of a scratch card worth 1 crore. Send your bank details to claim.", "lottery_prize"),
        d("KBC lucky draw! Aapne 25 lakh jeete hain. Inaam paane ke liye apna bank account number bhejein.", "lottery_prize"),
        d("आपकी लॉटरी लगी है, 10 लाख का इनाम। पहले 5000 टैक्स भेजें।", "lottery_prize"),
        d("Instant loan approved! No documents. Pay processing fee 999 to get 50000 in your account.", "loan_app"),
        d("Your loan app EMI is overdue. Our recovery agent will call your contacts and make your morphed photos viral.", "loan_app"),
        d("लोन ऐप वाले कह रहे हैं कि फोटो वायरल कर देंगे, तुरंत पैसे भेजो।", "loan_app"),
        d("Your KYC is expiring today. Update now at http://sbi-kyc-update.xyz or your account will be blocked.", "refund_tax_bill"),
        d("Your GST refund of Rs 15,000 is pending. Update your bank account details to receive it: http://gst-refund.xyz", "refund_tax_bill"),
        d("Attention! Your SIM will be deactivated in 2 hours. Call 1800-000-000 to verify your Aadhaar number now.", "refund_tax_bill"),
        d("Dear user, to receive your refund please share your UPI PIN with our customer care.", "refund_tax_bill"),
        d("URGENT: Your electricity will be disconnected tonight. Call this number 9876543210 to pay your bill immediately.", "refund_tax_bill"),
        d("Aapki bijli ka bill pending hai. Aaj hi bhugtan karein warna connection kat jayega.", "refund_tax_bill"),
        d("Your account will be suspended today. Verify your KYC by clicking the link in this message.", "refund_tax_bill"),
        d("Your debit card is suspended. To reactivate, share your card number and CVV with our officer.", "credential"),
        d("Dear sir, I am from SBI bank. Your debit card is about to expire. Share your card number, expiry and CVV to renew.", "credential"),
        d("Dear customer your ATM card is blocked. To unblock send OTP to this number.", "credential"),
        d("Please share your OTP with our customer care to complete verification and avoid account block.", "credential"),
        d("Security update: confirm your CVV and UPI PIN to reactivate your account immediately.", "credential"),
        d("Scan this QR code to receive your refund of 2999. Enter your UPI PIN after scanning.", "refund_tax_bill"),
        d("An unknown number video-called you and recorded it. Pay 50000 or we will make your private video viral.", "romance_sextortion"),
        d("Aapki video वायरल कर देंगे agar aap paise nahi bhejte. 20000 transfer karo.", "romance_sextortion"),
        d("You have a missed delivery. Pay the customs clearance fee of Rs 500 to release your parcel.", "courier_delivery"),
        d("Your parcel is held at customs. Pay clearance charges of Rs 5000 to release it.", "courier_delivery"),
        d("For any bank problem call our customer care number 9876543210 immediately to fix your account.", "fake_customer_care"),
        d("Your electricity will be disconnected. Call the toll free number to pay the pending bill.", "refund_tax_bill"),

        // ================= SCAMS — evasion tricks =================
        d("Your K Y C is expiring today. Update now at http://sbi-kyc-update.xyz", "refund_tax_bill"),
        d("Your account is blocked. Share your O T P with our officer to reactivate.", "credential"),
        d("Dear customer, your K.Y.C is expiring. Click the link or your account will be blocked.", "refund_tax_bill"),
        w("Please shаre your OTP with our team to verify. (look-alike letters)", "credential"),
        d("Aapka KYC expire ho gaya hai. Account band ho jayega. Turant is link par click karein.", "refund_tax_bill"),
        d("Aapka account block ho gaya hai. OTP batao warna paisa nikal jayega.", "credential"),
        d("Sir aapka account block ho gaya hai. KYC update karne ke liye is number par call karein.", "refund_tax_bill"),
        d("Bank se bol rahe hain. Aapka card block ho gaya hai. CVV aur PIN bhejo.", "credential"),
        d("You are under digital arrest. Call 1930 to verify. Do not hang up.", "digital_arrest"),
        d("Congratulations! Your number was selected in a lucky draw. To get the prize, install AnyDesk and share your screen.", "lottery_prize"),
        d("Install AnyDesk so our officer can help you with your bank KYC remotely.", "refund_tax_bill"),
        w("Hello, I am calling from the bank. Please tell me your OTP to fix your account problem.", "credential"),
        d("Aapka KYC update nahi hua to account band ho jayega, abhi is link par click karo.", "refund_tax_bill"),
        d("Your SIM will be blocked as it is linked with an illegal Aadhaar. Call this number to verify.", "refund_tax_bill"),
        d("You have an unpaid tax. A warrant will be issued. Pay now to avoid arrest.", "digital_arrest"),
        d("Aapko 50,000 ka profit hua hai, nikalne ke liye pehle 10,000 tax bhejo.", "investment"),
        d("Join our premium investment plan, 10% monthly return guaranteed. WhatsApp us.", "investment"),
        d("Your loan EMI is pending. Recovery team will visit your home and inform your family. Pay immediately.", "loan_app"),
        d("We will make your photos viral if you do not pay the loan amount today.", "loan_app"),
        d("आपका खाता बंद हो जाएगा। तुरंत केवाईसी अपडेट करें।", "refund_tax_bill"),
        d("Aapke paas parcel hai jisme nasha hai. Police case ho jayega. 20000 bhejo.", "courier_delivery"),
        d("Aapko digital arrest kiya gaya hai. Video call par rahiye aur paise transfer karein.", "digital_arrest"),
        d("Aapke bete ka accident ho gaya hai. Yeh naya number hai. Turant 30000 bhejo.", "family_emergency"),
        d("Mummy main dost ke phone se bol raha hoon, hospital me hoon, paise bhejo jaldi.", "family_emergency"),
        d("Your refund is pending. Click here and enter your bank details to claim it.", "refund_tax_bill"),
        d("Urgent: your card is blocked. Call our helpline number and share your card details.", "credential"),
        d("CBI officer here. A parcel in your name has drugs. Pay 50000 to close the case, do not tell anyone.", "digital_arrest")
    )
}
