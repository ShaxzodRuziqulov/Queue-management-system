# Telegram support

Backend reads `.env` in its working directory. Set TELEGRAM_BOT_TOKEN,
TELEGRAM_BOT_USERNAME and TELEGRAM_SUPPORT_CHAT_IDS (comma-separated private operator IDs).
Operator: open bot, send `/id`, put the numeric ID in env, restart backend.
Only configured private operator chats can reply. Groups are unsupported.

Frontend: `/help` -> Telegram button -> Start. Link expires after ten minutes.
User text, photos and files are forwarded. Operator replies with text:

    /reply USER_UUID Your reply

Keep exactly one backend polling instance running per bot token, with no webhook.
Cloud Run needs a continuously running worker for this polling implementation.
Set `telegram.bot.enabled=false` in secondary instances.

This version uses Telegram history, not a ticket database. Partial delivery retries
can duplicate messages. Booking/payment event notifications are not connected yet.
Test with separate user and operator accounts before production use.
