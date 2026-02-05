# TODO: Implement Event Reminders Display from MySQL Database

## Tasks
- [x] Update event-reminders.html to fetch upcoming reminders from /api/reminders/upcoming instead of directly from bookings
- [x] Adjust the reminder display to show event title and offset label (e.g., "3 days before")
- [ ] Ensure reminders are created and stored in MySQL via ReminderScheduler or BookingController
- [ ] Test the reminder display functionality

## Notes
- ReminderScheduler already creates EventReminder entries for booked events.
- ReminderController /upcoming endpoint fetches these from DB.
- Frontend currently fetches bookings; change to use reminders API.
