## Chat Service (1-to-1)

Enables private conversations between two users.

- Creates a chat room between two people
- Stores messages sent between users
- Keeps track of who sent each message and when
- Figures out who the receiver is based on the sender


## Authentication Service

It protects the app from unauthorised users, acts like a guard for our app or in more simmple words this service checks whether a user is allowed to use the app or not.

- It checks if the user is already logged in or not
- It verifies wheter the user is a valid user or not
- It allows access to the app only if the user is trusted and has a token(jwt token, used for authorization)
- It blocks the user from accessing the app if the user is not recognized or the token mismatches
- It simply protects the app from unauthorized usage


## Aura Service

It tells about the detials about the specified user. User has the fields - skills, education, techStack, languages, frameworks, communicationSkills, certifications, projects, softSkills, hobbies, experience, internships etc.

- It recieves a user id.
- It verifies wheter the user with that user id is present.
- It reads the data, and responds with all the fields.
- It returns empty list for a field, if field is null, or empty. 


## Resume Service

Enables the user to upload their resume, and get the resume based on the user id.

- To upload a resume, it gets the user id, and the resume file.
- It verifies wheter the file is less than 5MB, and of type pdf or document.
- It reads the data, and responds with all the fields.
- It also returns with the resume file, based on the user id.

## Job Post Service

Allows users (especially alumni) to share job opportunities with the community.

- Lets alumni create job posts with all the details.
- Stores information about internships, full-time jobs, part-time jobs, or contract work  
- Validates that the post has proper details like minimum 50 characters description
- Checks if image URLs are valid when users add pictures to job posts
- Helps students discover career opportunities shared by alumni

## Networking Service
 
-Helps users connect with each other, like making friends or following someone.
-Keeps track of who is connected to whom in the app.
-Lets users send and accept connection requests.
-Works quietly in the background so other features like chat or profile suggestions know your connections.
-Makes it easy to build a network of people inside the app.
