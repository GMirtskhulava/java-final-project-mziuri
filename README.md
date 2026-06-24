# SocNet

SocNet is a JavaFX social network desktop application. It supports account registration, login, password recovery, posts, likes, shares, friends, private messages, profile privacy, blocked users, and a Pong mini-game with a leaderboard.

SocNet არის JavaFX-ზე შექმნილი სოციალური ქსელის desktop აპლიკაცია. აპლიკაციას აქვს რეგისტრაცია, ავტორიზაცია, პაროლის აღდგენა, პოსტები, მოწონება, გაზიარება, მეგობრები, პირადი შეტყობინებები, პროფილის privacy პარამეტრები, დაბლოკილი მომხმარებლები და Pong მინი-თამაში leaderboard-ით.

## Tech Stack / ტექნოლოგიები

- Java 25
- Maven
- JavaFX
- MySQL Connector
- Jakarta Mail / Angus Mail 2.0.3
- FXML + CSS

## Project Structure / პროექტის სტრუქტურა

- `src/main/java/org/example/finalproject` - main application classes, models, utilities, database, mailer, token/session logic. | აპლიკაციის მთავარი მოდულები/კლასები
- `src/main/java/org/example/finalproject/controllers` - JavaFX controllers for every screen. | JavaFX სცენების კონტროლერები
- `src/main/resources/org/example/finalproject` - FXML screens and `styles.css` | JavaFX სცენები (UI) და სტილიზაცია (css)
- `data/` - serialized local files, for example MySQL config and session token. | მონაცემები/ქეში: MySQL კონფიგურაცია და აქტიური სესიის ტოკენი

## Run / გაშვება

1. Install JDK 25 or a compatible JDK for the configured Maven compiler.
2. Install and start MySQL locally. 
3. Run the app:
#
At first launch, app opens the MySQL configuration screen. Enter port, user, and password. The app saves this configuration in `data/mysql.ser`, connects to MySQL, creates the `socnet_javafx` database, creates all required tables, inserts demo data if the database is empty, and then opens the login page.

პირველ გაშვებაზე აპლიკაცია ხსნის MySQL-ის კონფიგურაციის გვერდს. შეიყვანეთ პორტი, მომხმარებელი და პაროლი. აპლიკაცია ინახავს კონფიგურაციას `data/mysql.ser` ფაილში, უკავშირდება MySQL-ს, ქმნის `socnet_javafx` ბაზას, საჭირო ცხრილებს, ცარიელ ბაზაში ამატებს სატესტო მონაცემებს და შემდეგ ხსნის ავტორიზაციის გვერდს.

## Database / მონაცემთა ბაზა

`MySQL.InitDB()` creates tables:

- `users` - user profile data, login contact, MD5 password, privacy settings, and total Pong score.
- `access_tokens` - serialized login sessions with expiration date.
- `posts` - user posts.
- `post_likes` - likes for original/shared posts.
- `post_shares` - post shares by users.
- `blocks` - blocked users.
- `friends` - friends (requests/accepted).
- `messages` - private chat messages with `seen` status.
- `pinned_chats` - pinned conversations per user.

`MySQL.InitDB()` ქმნის შემდეგ ცხრილებს:

- `users` - მომხმარებლის მონაცემები: საკონტაქტო ინფორმაცია, პაროლი (MD5 შიფრით), კონფიდენციალურობის პარამეტრები და თამაშების ქულები.
- `access_tokens` - ავტორიზაციის ტოკენები + აქტიურობის ვადებით.
- `posts` - მომხმარებლის პოსტები.
- `post_likes` - ორიგინალი/გაზიარებული პოსტების მოწონებები.
- `post_shares` - გაზიარებული პოსტები.
- `blocks` - დაბლოკილი მომხმარებლები.
- `friends` - მეგობრობები (მოთხოვნები/დადასტურებულები).
- `messages` - პირადი შეტყობინებები `seen` სტატუსით.
- `pinned_chats` - ჩამაგრებული ჩატები.

## Main Features / მთავარი ფუნქციები

- Authentication: login, registration, password recovery by email code, token-based session restore.
- Feed: create posts, view latest posts and shares, like/dislike, share, delete own posts/shares.
- Profiles: view bio/contact info, posts, shared posts, friend/posts count, message button, block/unblock.
- Friends: view friends, approve/decline requests, remove friends, respect private friend lists.
- Messages: chat list, direct chat, unread counters, seen status, pinned chats, automatic polling for new messages.
- Search: search people and posts, open profiles, send friend requests.
- Settings: edit profile, password, privacy options, blocked users, account deletion.
- Games: Pong game and leaderboard based on total user score.


- ავტორიზაცია: ავტორიზაცია, რეგისტრაცია, პაროლის აღდგენა მეილის კოდით, ტოკენები სესიების აღსადგენად.
- მთავარი: პოსტის შექმნა, პოსტებისა და გაზიარებების ნახვა, მოწონება/დაწუნება, პოსტის გაზიარება, საკუთარი პოსტების (მათ შორის გაზიარებულის პოსტების) წაშლა.
- პროფილები: საკონტაქტო ინფო (მეილი ან ნომერი), ბიო, პოსტები, გაზიარებები, მეგობრების/პოსტების რაოდენობა, შეტყობინების ღილაკი, დაბლოკვა/განბლოკვა
- მეგობრები: მეგობრების სია, მოთხოვნის დადასტურება/უარყოფა, მეგობრებიდან წაშლა, პრივატული მეგობრების სია.
- შეტყობინებები: ჩატების სია, წაუკითხავი მესიჯების რაოდენობა, seen სტატუსი, აპინული ჩატები, ახალი შეტყობინებების LIVE მიღება.
- ძიება: ადამიანებისა და პოსტების ძიება "გასაღები" სიტყვით, პროფილის გახსნა, მეგობრობის გაგზავნა.
- პარამეტრები: პროფილის, პაროლისა და კონფიდენციალურობის პარამეტრების შეცვლა, დაბლოკილი მომხმარებლების მართვა, ანგარიშის წაშლა.
- თამაშები: Pong თამაში და leaderboard.

## Function Guide / ფუნქციების აღწერა

### Application and Core / აპლიკაცია და ძირითადი კლასები

#### `MySQL`

- `ConnectToDatabase()` - connects to local MySQL using configured port, username, and password. | ლოკალურ MySQL-თან დაკავშირება
- `InitDB()` - creates the database and all application tables if they do not exist. | ცხრილების ინიციალიზირება
- `initTestData()` - inserts demo users and posts when tables are empty. | სატესტო მონაცემების შეყვანა

#### `MySQLConfig`

- Constructors store MySQL username/password or port/username/password.
- ინახავს MySQL-ის კონფიგურაციას, რომელიც შემდეგ `data/mysql.ser`-ში სერიალიზირდება.

#### `Utils`

- `showFriendRequestsMark(Button friendsButton)` - adds a visual mark to the Friends button when pending requests exist. | თუ შემოსულია მეგობრობის მოთხოვნა, აჩვენებს ვიზუალურად (წერტილით).
- `changeScene(ActionEvent event, String fxmlName)` - changes screen using the event source. | ცვლის სცენას event დახმარებით
- `changeSceneFromNode(Node node, String fxmlName)` - changes screen using any JavaFX node and updates window title. | სცენის ცვილება node-ით 
- `serializeObject(Object object, String path)` - saves an object into `data`. | Object სერიალიზაცია data ფოლდერში.
- `deserializeObject(String path)` - reads a serialized object from `data`. | Object დესერიალიზაცია data ფოლდერიდან.
- Static fields `searchText`, `profileUserID`, `friendsUserID`, `messageUserID` - pass selected state between controllers. | კონტროლერებს შორის ინფორმაციის "მიმოცვლის" ცვლადები

#### `Token`

- `Token(int userID)` - generates a token, stores it in `access_tokens`, sets a 2-hour expiration, and serializes it to `data/session.ser`. | რენდომ ტოკენის გენერაცია, მონაცემთა ბაზაში (`access_token`) დამახსოვრება, 2 საათიანი აქტიურობის დაყენება და სერიალიზაცია `data/session.ser` ფაილში

- `generateToken(int ID)` - creates a random token string. | აგენერირებს რენდომ ტოკენს სტრინგის სახით
- `validateToken(Token token)` - checks token existence and expiration in the database; expired tokens are deleted. | ამოწმებს ტოკენის ვალიდურობას (მოქმედების ვადას) ბაზაში.
- `serializeToken(Token token)` - writes the token to disk. | ინახავს ტოკენს კომპიუტერში
- `deserializeToken()` - reads the token from disk. | კითხულობს ტოკენს

#### `Mailer`

- `Mailer()` - initializes SMTP session for Gmail. | SMTP სესიის დაინიციალიზირება (gmail-სთვის)
- `sendEmail(String recipient, String message_title, String message_content)` - sends an HTML email. | შეტყობინების გაგზავნის ფუნქცია (HTML სახით)
- `isEmail(String text)` - checks whether text looks like an email address. | ელ .ფოსტის ვალიდურობის შემოწმება



#### `User`

- Constructors create user objects.
- კონსტრუქტორი ქმნის მომხმარებლის object-ს 
- `checkUser(String contactInfo, String password)` - authenticates user by contact info and MD5 password. | ამოწმებს მომხმარებლის ვალიდურობას (ავტორიზაციისას)
- `logoutActiveUser()` - deletes the active access token and clears serialized session data. | გამოყავს სისტემიდან მომხმარებელი

#### `Post`

- Constructors create existing or new post objects.
- კონსტრუქტორი ქმნის პოსტს.
- `createUserPost(User user, Post post)` - validates active user and content length, then inserts a new post. | ახდენს მომხმარებლის და პოსტის ვალიდაციას (სიგრძს მიხედვით), შემდეგ ამატებს ბაზაში 


#### `ChatUser`

- Constructor stores chat user ID, full name, last message, unread count, and pinned status.
- კონსტრუქტორი ქმნის მომხმარებლებს მესიჯების სიისთვის

#### `ChatMessage`

- Constructor stores sender ID, content, seen status, and creation date.
- კონსტრუქტორი ქმნის შეტყობინებების Object-ს

### Controllers / კონტროლერები

Most controllers contain repeated navigation functions | კონტროლერებში ხშირად შევხვდებით განმეორებად ფუნქციებს:

- `showFeed`, `showProfile`, `showMessages`, `showFriends`, `showGames`, `showSettings`, `showSearch` - switch between main screens and save needed state in `Utils` | სცენებს შორის ნავიგაცია და `utils`-ში სტატიკური ინფორმაციის შენახვა. 
- `openMyProfile` - opens the current user profile. | აქტიური მომხმარებლის პროფილის გახსნა
- `handleLogOutButton` - logs out the active user and returns to login. | ანგარიშიდან გამოსვლა
- `getDisplayName` / `getFullName` - formats names safely. | აერთიანებს მომხმარებლის სახელს და გვარს

#### `MySQLConnectController`

- `handleExitButton()` - აპლიკაციის დახურვა.
- `handleSaveButton(ActionEvent event)` - saves MySQL config, connects to DB, initializes schema/demo data, and opens login. | ინახავს მითითებულ MySQL მონაცემებს ფაილში სერიალიზაციის დახმარებით.


#### `LoginController`

- `handleLoginButton(ActionEvent event)` - validates form, authenticates user, creates token, sets current user, opens feed. | ავტორიზაციის ინფორმაციის ვალიდაცია, მომხმარებლის ავტორიზაცია, ტოკენის შექმნა
- `navigateToRegister(ActionEvent event)` - opens registration page. | რეგისტრაციაზე გადაყვანა
- `navigateToRecovery(ActionEvent event)` - opens password recovery page. | პაროლის აღდგენაზე გადაყვანა
- `validateForm()` - validates contact info and password fields. | შეყვანილი ინფორმაციის ვალიდაცია

#### `RegisterController`

- `handleRegisterButton(ActionEvent event)` - validates form, checks duplicate contact info, inserts user, logs in user, sends welcome email for email contacts. | რეგისტრაციის მონაცემების ვალიდაცია, ახალი მომხმარებლის ბაზაში შექმნა, მომხმარებლის ავტორიზაცია და შეტყობინების გაგზავნა მეილზე.
- `navigateToLogin(ActionEvent event)` - opens login page. | ავტორიზაციის გვერდზე გადასვლა.
- `validateForm()` - validate inputs | შეყვანილი ინფორმაციის ვალიდაცია

#### `RecoveryPasswordController`

- `handleSendCode(ActionEvent event)` - finds user by contact info, generates a 6-digit code, and sends recovery email. | კოდის გაგზავნის ღილაკი, ადასტურებს მეილს (არის თუ არა ბაზაში), აგენერირებს 6 ციფრიან კოდს და გზავნის მეილზე.
- `handleConfirmCode(ActionEvent event)` - checks entered code and opens new password step. | ამოწმებს შეყვანილი კოდის სისწორეს.
- `handleResetPassword(ActionEvent event)` - validates new password and updates password with MD5 | უკეთებს ვალიდაციას ახალ პაროლს და ანახლებს ბაზაში მომხმარებლის პაროლს.
- `navigateToLogin(ActionEvent event)` - opens login page. | ავტორიზაციის გვერდზე გადასვლა.

#### `MainController`

- `handlePostButton(ActionEvent event)` - creates a post  | ქმნის ახალ პოსტს და 
- `handlePostClearButton()` - clears post text and error message. | ასუფთავებს შეყვანილ ტექსტს ახალი პოსტის შესაქმნელ ველში + error-შეტყობინებას
- `loadSuggestions()` - loads friend-of-friend suggestions excluding blocked users. | ტვირთავს მეგობრების შეთავაზებებს (მეგობრების მეგობრებს. ვინც დაბლოკილი/უკვე მეგობარი არ არის)
- `loadFeedPosts()` - loads recent posts and shares, excluding blocked users. | ტვირთავს აქტუალურ პოსტებს (გარდა ბლოკირებული მომხმარებლის პოსტებისა)
- `makePostBox(...)` - builds a post/share UI card | ქმნის UI-ს პოსტისთვის
- `getPostCounters(int postID, int shareID)` - counts likes and shares. | ითვლის პოსტის  ლაიქებს და გადაზიარებებს
- `userLikedPost(int postID, int shareID)` - checks whether current user liked a post/share | ამოწმებს მოწონებული აქვს თუ არა აქტიურ მომხმარებელს პოსტი 
- `toggleLikePost(int postID, int shareID)` - adds/removes a like | პოსტის მოწონება/დაწუნება 
- `sharePost(int postID)` - creates a share for the current user. | აზიარებს პოსტს (ამატებს ბაზაში ინფორმაციას `post_shares`)
- `deletePost(int postID)` - deletes own post | შლის აქტიური მომხმარებლის პოსტს + ლაიქებს/გადაზიარებებს
- `deleteShare(int shareID)` - deletes own share and related likes | შლის აქტიური მომხმარებლის გადაზიარებულ პოსტს 
- `sendFriendRequest(int userID, Button button)` - sends a pending friend request. | აგზავნის მეგობრობის მოთხოვნას მომხმარებელთან

#### `ProfileController`

- `showMessages(ActionEvent event)` - opens messages; from profile button it preselects that profile user | გადადის არჩეული მომხმარებლის შეტყობინებებში
- `openProfileFriends(ActionEvent event)` - opens selected user's friends list. | ხსნის არჩეული მომხმარებლის მეგობრების სიას
- `handleFriendActionButton()` - sends/cancels/removes/approves friendship depending on current status. | აგზავნის/ადასტურებს/უარყოფს/შლის მეგობრობის მოთხოვნას
- `handleBlockButton()` - blocks or unblocks the profile user and removes friendship on block. | ბლოკავს/განბლოკავს მომხმარებელს
- `loadMyPosts()` - loads profile user's posts and shares, or hides them when blocked | ტვირთავს მომხმარებლის პოსტებს (ან მალავს ბლოკირების შემთხვევაში)
- `makePostBox(...)` - builds profile post/share UI. | ქმნის პოსტის UI-ს
- `loadProfileInfo()` - loads profile name, bio, contact info visibility, message availability, block state. | ტვირთავს პროფილის ინფორმაციას: სახელი, გვარი, ბიო, საკონტაქტო ინფო., შეტყობინების გაგზავნა, ბლოკირების სტატუსი
- `loadBlockStatus()` - checks whether either user blocked the other. | ამოწმებს დაბლოკილია თუ არა მომხმარებელი
- `getFriendStatus()` - returns `accepted`, `pending`, or `none` and tracks request direction. | ამოწმებს მეგობრობის სტატუსს (გაგზავნილია/შემოსულია მოთხოვნა თუ არცერთი)
- `loadFriendsCounter()` - counts accepted friends for profile. | იტვლის მეგობრებს


#### `MessagesController`

- `handleSendMessage()` - validates message length, checks permission, inserts message, reloads chat and list | ახდენს გასაგზავნი შეტყობინების ვალიდაციას, აგზავნის შეტყობინებას (ინახავს მონაცემთა ბაზაში, ანახლებს UI-ს)
- `loadUserList()` - builds chat list from pinned chats, accepted friends, and users with previous messages | ტვირთავს მომხმარებლებს: აპინული ჩატებიდან, მეგობრებიდან, ვისთანაც მიწერილი გვაქვს/აქვს მოწერილი
- `getLastMessage(int userID)` - returns the newest message content for a chat | აბრუნებს ბოლო გაგზავნილ/შემოსულ შეტყობინებას
- `getUnreadMessages(int userID)` - counts unseen incoming messages | ითვლის ახალ, უნახავ შეტყობინებებს
- `makeUserBox(ChatUser user)` - builds a chat list row with pin mark, unread label, and active styling | ამატებს Ui-ში მომხმარებლის "ბარათს" (სახელი გვარი, ბოლო შეტყობინება)
- `openChat(int userID, String fullname, boolean pinned)` - selects conversation| ტვირთავს ჩატს არჩეულ მომხმარებელთან
- `openUserWithload(int userID)` - opens a requested user who is not already in the chat list | ხსნის ჩატს მომხმარებელთან, რომელიც სიაში არ არის
- `loadMessages(int userID, String fullname)` - loads conversation messages, marks incoming messages as seen, scrolls to bottom | ტვირთავს შეტყობინებებს, ანახლებს `seen` სტატუსს
- `makeMessageBox(ChatMessage message)` - builds incoming/outgoing message bubbles with sent/seen status | არენდერებს UI-ში გაგზავნილ/შემოსულ შეტყობინებებს
- `showSystemMessage(String text)` - shows a system label in the messages area | აჩვენებს სისტემურ შეტყობინებას მესიჯებთან
- `canMessage(int userID)` - returns whether messaging is allowed, blocked, or unavailable by privacy | აბრუნებს სტატუსს, შეუძლია თუ არა მომხმარებელს შეტყობინების გაგზავნა არჩეულ მომხმარებელთან (1 - შეუძლია | 0/2 - არ შეუძლია/ბლოკირებულია)
- `toggleChatPin(ActionEvent event)` - pins or unpins selected chat | ამაგრებს/ხსნის ჩამაგრებიდან ჩატეს
- `checkingNewMessages()` - checks DB for new incoming messages in selected chat | ახალი მესიჯების შემოწმება ბაზაში
- `startGettingMessages()` - starts background polling every 3500ms | იძახებს checkNewMessages ფუნქციას ყოველ 3,5 წამში რათა მიიღოს ახალი შეტყობინებები
- `stopGettingMessages()` - stops polling when leaving the page | თიშავს ახალი შეტყობინებების მიღების ციკლს.

#### `SearchController`

- `loadSearchResults()` - searches users by name/contact and posts by content | ძებნის მომხმარებლებს/პოსტებს მითითებული სიტყვით
- `showResults(...)` - renders people and post results | არენდერებს მოძებნილ მომხმარებლებს/პოსტებს
- `makeUserRow(...)`, `makePostBox(...)` - builds UI | არენდერებს UI მხარეს (მოძებნილი მომხმარებლების/პოსტებს)
- `getFriendStatus(int userID)` - checks friendship status with a result user | აბრუნებს მეგობრობის სტატუსს მომხმარებელთან
- `sendFriendRequest(int userID, Button button)` - sends request and updates button state | აგზავნის მეგობრობის მოთხოვნას მომხმარებელთან

#### `SettingsController`

- `loadMySettings()` - loads current user fields and privacy options into the form. | ტვირთავს აქტიური მომხმაებლის პარამეტრებს და შეყავს ინფორმაცია ველებში
- `handleSaveButton()` - validates and saves profile fields, privacy flags, and optional new password. | ახდენს შეყვანილი ინფორმაციის ვალიდაციას და ინახავს ბაზაშია ახალ ინფორმაციას
- `handleDeleteAccountButton(ActionEvent event)` - confirms and deletes account data, tokens, relations, messages, blocks, and user row. | ითხოვს დადასტურებას და შემდეგ შლის ანგარიშს
- `loadBlockedUsers()` - loads users blocked by current user. | აბრუნებს დაბლოკილ მომხმარებლებს
- `makeBlockedUserRow(...)` - builds blocked-user row with unblock action. | UI-ში ამატებს დაბლოკილ მომხმარებლებს განბლოკვის ღილაკით
- `unblockUser(int userID)` - removes a block entry. | განბლოკავს მომხმარებელს

#### `GamesController`

- `openPongGame(ActionEvent event)` - opens Pong screen. | Pong-ის გახსნა
- `loadLeaderboard()` - loads top users ordered by total Pong score. | ტოპ მოთამაშეების ჩატვირთვა თავისი ქულებით

#### `PongController`

- `handleGameStart()` - starts the Pong animation loop and keyboard-controlled gameplay. | თამაშის დაწყება, რთავს animation loop-ს
- `configureGameProperties()` - sets paddle sizes, ball radius, speeds, positions, and initial score labels. | აკონფიგურირებს საწყის ჩოგნების და ბურთის პარამეტრებს (ზომა, სიგრძე/სიგანე, სისწრაფე და ა.შ) 
- `updateScore(String side)` - updates scores and changes difficulty as blue score increases. | ანახლებს მხარეების ქულებს
- `setPaddleHeight(Rectangle paddle, double newHeight)` - changes paddle height. | ანახლებს ჩოგნის სიმაღლეს
- `handleBackButton(ActionEvent event)` - stops game, saves current score into `totalPongScores`, and returns to games page. | უკან დაბრუნების ღილაკი: აჩერებს თამაშს, ინახავს მიმდინარე ქულას ბაზაში (აჯამებს ძველ ქულასთან)


## Notes / შენიშვნები

- Passwords are stored with `MD5(...)` in MySQL | პაროლები MySQL-ში `MD5(...)`-ით ინახება.



## Test Accounts / სატესტო ანგარიშები

| Name | Login | Password |
|------|--------|----------|
| Giorgi Test | `giorgi@test.com` | `123456` |
| Nino Player | `nino@yahoo.com` | `123456` |
| Luka Beridze | `luka@gmail.com` | `123456` |
| Mariam Mariam | `555123321` | `123456` |



### Presentation / პრეზენტაცია
ქართულად (Georgian): https://docs.google.com/presentation/d/1NrmoIGTp_yYIfNqTpiNSAwYQXfAWDU-l9sGD9Jvev3I