package com.bharatsuraksha.saathi

/** How a sample should be judged. */
enum class Kind {
    /** Must come out SAFE. */
    SAFE,

    /** Must NOT be SAFE (CAREFUL or DANGEROUS). */
    SCAM,

    /** Must come out DANGEROUS. */
    STRONG
}

data class Sample(val text: String, val kind: Kind)

/**
 * A corpus of 54 real-style messages: bank notices, family chats, Hinglish scam
 * texts, "digital arrest" calls, phishing links, loan-app threats and more.
 * Mix of English, Hindi-transliterated (Hinglish) and Devanagari.
 */
object ScamSamples {

    val all: List<Sample> = listOf(
        // ---------------- SAFE ----------------
        Sample("Hi, are we still meeting for lunch at 1pm?", Kind.SAFE),
        Sample("Your order #A1234 has been shipped and will reach you tomorrow.", Kind.SAFE),
        Sample("Mom, I'll be late today, please keep dinner aside.", Kind.SAFE),
        Sample("Dear customer, your account statement for January is ready in the app.", Kind.SAFE),
        Sample("Reminder: dentist appointment tomorrow at 10am.", Kind.SAFE),
        Sample("Team, the meeting is moved to 4pm. Please update your calendars.", Kind.SAFE),
        Sample("Congratulations to Rohan for winning the office quiz!", Kind.SAFE),
        Sample("Your OTP for login is 4821. Do not share it with anyone.", Kind.SAFE),
        Sample("Airtel: Your data pack expires on 30 Jan. Recharge to continue.", Kind.SAFE),
        Sample("Your package is out for delivery today.", Kind.SAFE),
        Sample("Dad, I reached college safely.", Kind.SAFE),
        Sample("Please find attached the invoice for December.", Kind.SAFE),
        Sample("Your appointment with Dr. Sharma is confirmed for Monday.", Kind.SAFE),
        Sample("Happy Diwali! Wishing you and your family joy and prosperity.", Kind.SAFE),
        Sample("Mobile bill of Rs 499 is due on 15 Jan. Pay via the official app.", Kind.SAFE),
        Sample("Aapka OTP kisi ko na batayein. Bank kabhi OTP nahi maangta.", Kind.SAFE),

        // ---------------- STRONG (must be DANGEROUS) ----------------
        Sample(
            "Dear customer, your KYC is expiring today. Update now at http://sbi-kyc-update.xyz or your account will be blocked.",
            Kind.STRONG
        ),
        Sample(
            "CBI notice: a parcel with illegal items is in your name. You are under digital arrest. Call this number immediately.",
            Kind.STRONG
        ),
        Sample(
            "Income Tax Department: You have an unpaid tax. A non-bailable warrant has been issued. Pay immediately or you will be arrested.",
            Kind.STRONG
        ),
        Sample(
            "Congratulations! You have won Rs 25 lakh in the KBC lucky draw. Click here to claim your prize: bit.ly/xyz",
            Kind.STRONG
        ),
        Sample(
            "Work from home! Earn Rs 5000 per day. Registration fee Rs 500 only. Pay now and start today.",
            Kind.STRONG
        ),
        Sample(
            "Dear customer, your debit card is suspended. To reactivate, share your card number and CVV with our officer.",
            Kind.STRONG
        ),
        Sample(
            "Your KYC has expired. Your account will be deactivated within 24 hours. Update here: http://hdfcbank.verify-kyc.com",
            Kind.STRONG
        ),
        Sample(
            "You are selected for a part time job. Earn 2000 daily. Pay joining fee 200 to confirm.",
            Kind.STRONG
        ),
        Sample(
            "Congratulations! You are the lucky winner of a scratch card worth 1 crore. Send your bank details to claim.",
            Kind.STRONG
        ),
        Sample(
            "You have received a WhatsApp from the CBI. There is an FIR against you for money laundering. Transfer 50,000 to close the case.",
            Kind.STRONG
        ),
        Sample(
            "Instant loan approved! No documents. Pay processing fee 999 to get 50000 in your account.",
            Kind.STRONG
        ),
        Sample(
            "Click this link to update your PAN card or your account will be suspended: http://incometax-pan.xyz",
            Kind.STRONG
        ),
        Sample(
            "You are under digital arrest. Do not disconnect the video call. Transfer money to the government account for verification.",
            Kind.STRONG
        ),
        Sample(
            "Dear sir, I am from SBI bank. Your debit card is about to expire. Share your card number, expiry and CVV to renew.",
            Kind.STRONG
        ),
        Sample(
            "Your account has been frozen by the police due to suspicious activity. Contact this number to avoid legal action.",
            Kind.STRONG
        ),
        Sample(
            "Your GST refund of Rs 15,000 is pending. Update your bank account details to receive it: http://gst-refund.xyz",
            Kind.STRONG
        ),
        Sample(
            "We found illegal items in a parcel sent to you from Dubai. Police case has been registered. Pay 25000 to settle.",
            Kind.STRONG
        ),
        Sample(
            "This is a final warning from Income Tax. Pay the outstanding tax or a warrant will be issued in your name.",
            Kind.STRONG
        ),
        Sample(
            "Congratulations! Your number was selected in a lucky draw. To get the prize, install AnyDesk and share your screen.",
            Kind.STRONG
        ),
        Sample(
            "Dear customer your ATM card is blocked. To unblock send OTP to this number.",
            Kind.STRONG
        ),
        Sample(
            "You have won a prize of Rs 50,000. Click the link below and enter your bank details.",
            Kind.STRONG
        ),
        Sample(
            "Aapka KYC expire ho gaya hai. Account band ho jayega. Turant is link par click karein.",
            Kind.STRONG
        ),
        Sample(
            "Aapke naam par parcel aaya hai jisme drugs hain. Police case ho jayega. Paise bhejo.",
            Kind.STRONG
        ),
        Sample(
            "Aapko digital arrest kiya gaya hai. Video call par rahiye aur paise transfer karein.",
            Kind.STRONG
        ),
        Sample(
            "KBC lucky draw! Aapne 25 lakh jeete hain. Inaam paane ke liye apna bank account number bhejein.",
            Kind.STRONG
        ),

        // ---------------- SCAM (must NOT be SAFE) ----------------
        Sample(
            "URGENT: Your electricity will be disconnected tonight. Call this number 9876543210 to pay your bill immediately.",
            Kind.SCAM
        ),
        Sample(
            "Your loan app EMI is overdue. Our recovery agent will call your contacts and make your morphed photos viral if you do not pay now.",
            Kind.SCAM
        ),
        Sample(
            "Guaranteed returns! Invest 10000 and get 20000 in 30 days. Join our telegram group now.",
            Kind.SCAM
        ),
        Sample(
            "Your parcel is held at customs for illegal items. Pay clearance charges of Rs 5000 to release it.",
            Kind.SCAM
        ),
        Sample(
            "Dear user, to receive your refund please share your UPI PIN with our customer care.",
            Kind.SCAM
        ),
        Sample(
            "Attention! Your SIM will be deactivated in 2 hours. Call 1800-000-000 to verify your Aadhaar number now.",
            Kind.SCAM
        ),
        Sample(
            "Your credit card has been blocked for security reasons. Call this number immediately to unblock.",
            Kind.SCAM
        ),
        Sample(
            "Lottery department: Your mobile number won 25 lakh in the draw. To claim, send 5000 as tax.",
            Kind.SCAM
        ),
        Sample(
            "Get free iPhone 15! Just pay delivery charges. Click here.",
            Kind.SCAM
        ),
        Sample(
            "URGENT: Your electricity connection will be cut off tonight at 9:30 PM. Call this number to pay your pending bill.",
            Kind.SCAM
        ),
        Sample(
            "Amazon refund: Your order was cancelled. Scan this QR code to receive your refund of 2999.",
            Kind.SCAM
        ),
        Sample(
            "Beware! Someone is using your Aadhaar. Send your Aadhaar photo to verify immediately.",
            Kind.SCAM
        ),
        Sample(
            "Your loan EMI is pending. Recovery team will visit your home and inform your family. Pay immediately.",
            Kind.SCAM
        ),
        Sample(
            "IMPORTANT: Your account will be suspended today. Verify your KYC by clicking the link in this message.",
            Kind.SCAM
        ),
        Sample(
            "Hello, I am calling from the bank. There is a problem with your account. Please tell me your OTP to fix it.",
            Kind.SCAM
        ),
        Sample(
            "Aapki bijli ka bill pending hai. Aaj hi bhugtan karein warna connection kat jayega.",
            Kind.SCAM
        ),
        Sample(
            "Sir aapka account block ho gaya hai. KYC update karne ke liye is number par call karein.",
            Kind.SCAM
        )
    )
}
