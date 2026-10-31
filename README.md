# Dark-Auction-for-Minecraft VAULT PLUGIN IS NEEDED
The plugin provides two distinct auction formats:

Live Bidding Auctions: A real-time competitive bidding war. Admins queue up to three unique items, and players bid against each other in an interactive GUI with quick-bid increment buttons (+100k, +500k, +1M, +5M coins). Each round features a 30-second countdown timer that dynamically extends when last-second bids arrive. When someone is outbid, their coins are refunded automatically in real time. Winning items are placed directly into the victor's inventory. Live auctions can be triggered manually by staff or set to run automatically every Saturday evening.
Display Mode (Showcase Market): A fixed-price, limited-supply showcase. Admins can display up to three items with set coin prices, individual or global stock counts, and per-player purchase limits (1 or 2 items max). Once an item's stock runs out, the GUI automatically marks it as "SOLD OUT" to prevent over-purchasing and keep rare items scarce.
Key Features:

Interactive NPC Auctioneer: Spawn a custom, invulnerable Cleric NPC (/da create) anywhere in your lobby or spawn. Players right-click the auctioneer to open the active auction or display GUI.
Vault Economy Support: Fully hooks into standard server economy providers via Vault for reliable balance tracking, instant deductions, and safe bid refunds.
Anti-Exploit Protection: Built-in safeguards automatically intercept commands like /pay, /money send, and direct player interactions while an auction is running, preventing mid-event coin transfers and unfair player collusion.
Admin Management: Simple in-game commands allow admins to configure items directly from their hand (/da item1, /da bid), manage stock
