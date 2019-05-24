package io.codelabs.digitutor.core.datasource.remote;


import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.local.UserSharedPreferences;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.core.util.Constants;
import io.codelabs.digitutor.core.util.InputValidator;
import io.codelabs.digitutor.data.BaseDataModel;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.data.model.*;
import io.codelabs.sdk.util.ExtensionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Firebase data source class
 * Required for all firebase transactions
 */
public final class FirebaseDataSource {

    // Login function: Email & password authentication
    public static void login(Activity host,
                             FirebaseAuth auth,
                             @NotNull LoginCredentials credentials,
                             @NotNull final AsyncCallback<Void> callback) {
        // Start process
        callback.onStart();

        if (credentials.validate()) {
            auth.signInWithEmailAndPassword(credentials.getEmail(), credentials.getPassword())
                    .addOnCompleteListener(host, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                            if (user != null) {
                                callback.onSuccess(null);
                                callback.onComplete();
                            }
                        } else {
                            callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                            callback.onComplete();
                        }
                    }).addOnFailureListener(host, e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });
        } else {
            callback.onError("There seems to be a problem with your credentials. Please check your email and password fields properly");
            callback.onComplete();
        }
    }

    public static void resetPassword(Activity host, FirebaseAuth auth, String email, @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        if (email != null && !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            auth.sendPasswordResetEmail(email).addOnCompleteListener(host, task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess(null);
                    callback.onComplete();
                } else {
                    callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                    callback.onComplete();
                }
            }).addOnFailureListener(host, e -> {
                callback.onError(Objects.requireNonNull(e.getLocalizedMessage()));
                callback.onComplete();
            });
            return;
        }

        callback.onError("Please enter a valid email address");
        callback.onComplete();
    }

    public static void uploadImage(@NotNull StorageReference storage, Uri uri, @NotNull AsyncCallback<String> callback) {
        callback.onStart();

        final StorageReference ref = storage.child(System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = ref.putFile(uri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                callback.onComplete();
            }

            // Continue with the task to get the download URL
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                callback.onSuccess(downloadUri != null ? downloadUri.toString() : null);
                callback.onComplete();
            } else {
                callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                callback.onComplete();
            }
        });
    }

    private static void uploadFile(@NotNull StorageReference storage, @NotNull Uri uri, @NotNull AsyncCallback<String> callback) {
        callback.onStart();

        final StorageReference ref = storage.child(uri.getLastPathSegment() + "" + System.currentTimeMillis());
        UploadTask uploadTask = ref.putFile(uri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                callback.onComplete();
            }

            // Continue with the task to get the download URL
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                callback.onSuccess(downloadUri != null ? downloadUri.toString() : null);
                callback.onComplete();
            } else {
                callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                callback.onComplete();
            }
        });
    }

    public static void updateUserAvatar(Activity host, @NotNull FirebaseFirestore firestore, @NotNull String type, String key, String avatar, @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        String collection = type.equals(BaseUser.Type.PARENT) ? Constants.PARENTS : Constants.TUTORS;

        // Create a hash map of the fields we are interested in
        HashMap<String, Object> hashMap = new HashMap<>(0);
        hashMap.put("avatar", avatar);
        hashMap.put("token", FirebaseInstanceId.getInstance().getToken());

        // Push data to the database
        firestore.collection(collection).document(key)
                .update(hashMap)
                .addOnCompleteListener(host, task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                    }
                    callback.onComplete();
                }).addOnFailureListener(host, e -> {
            callback.onError(e.getLocalizedMessage());
            callback.onComplete();
        });
    }

    /**
     * Create a new user account
     *
     * @param host        Calling Activity
     * @param auth        Firebase authentication
     * @param firestore   Database
     * @param credentials Login credentials
     * @param type        User type (Parent / Tutor
     * @param username    Username of account holder
     * @param callback    Callback for async process
     */
    public static void createUser(Activity host, FirebaseAuth auth, FirebaseFirestore firestore, @NotNull LoginCredentials credentials,
                                  String type, String username, @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        if (credentials.validate()) {
            auth.createUserWithEmailAndPassword(credentials.getEmail(), credentials.getPassword()).addOnCompleteListener(host,
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Create user model and set database collection path
                                String collection = type.equals(BaseUser.Type.PARENT) ? Constants.PARENTS : Constants.TUTORS;
                                BaseUser model = type.equals(BaseUser.Type.PARENT) ? new Parent() : new Tutor();
                                model.setName(username);
                                model.setEmail(credentials.getEmail());
                                model.setKey(Objects.requireNonNull(task.getResult()).getUser().getUid());
                                model.setType(model instanceof Parent ? BaseUser.Type.PARENT : BaseUser.Type.TUTOR);

                                // Store user information in  the database
                                firestore.collection(collection).document(Objects.requireNonNull(task.getResult()).getUser().getUid())
                                        .set(model).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        callback.onSuccess(null);
                                        callback.onComplete();
                                    } else {
                                        callback.onError(Objects.requireNonNull(task1.getException()).getLocalizedMessage());
                                        callback.onComplete();
                                    }
                                }).addOnFailureListener(e -> {
                                    callback.onError(e.getLocalizedMessage());
                                    callback.onComplete();
                                });
                            } else {
                                callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                                callback.onComplete();
                            }
                        }
                    }).addOnFailureListener(host, e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });
        } else {
            callback.onError("Invalid login credentials. Please check your email, username and password");
            callback.onComplete();
        }
    }

    public static void getCurrentUser(Activity host, FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, @NotNull AsyncCallback<BaseUser> callback) {
        callback.onStart();
        if (prefs.isLoggedIn()) {
            ExtensionUtils.debugLog(host, prefs.getType());
            String collection = prefs.getType().equals(BaseUser.Type.PARENT) ? Constants.PARENTS : Constants.TUTORS;
            firestore.collection(collection).document(prefs.getKey()).addSnapshotListener(host, (documentSnapshot, e) -> {
                if (e != null) {
                    callback.onError(e.getLocalizedMessage());
                    callback.onComplete();
                    return;
                }

                BaseUser user;
                if (prefs.getType().equals(BaseUser.Type.PARENT))
                    user = documentSnapshot.toObject(Parent.class);
                else {
                    user = documentSnapshot.toObject(Tutor.class);
                }

                // Send live data to the callback
                callback.onSuccess(user);
                callback.onComplete();
            });
        } else {
            callback.onError("Please sign in first");
            callback.onComplete();
        }
    }

    public static void getUser(Activity host, @NotNull FirebaseFirestore firestore, String key, @NotNull String type, @NotNull AsyncCallback<BaseUser> callback) {
        callback.onStart();
        String collection = type.equals(BaseUser.Type.PARENT) ? Constants.PARENTS : Constants.TUTORS;
        firestore.collection(collection).document(key).addSnapshotListener(host, (documentSnapshot, e) -> {
            if (e != null) {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
                return;
            }

            BaseUser user;
            if (type.equals(BaseUser.Type.PARENT))
                user = documentSnapshot.toObject(Parent.class);
            else {
                user = documentSnapshot.toObject(Tutor.class);
            }

            // Send live data to the callback
            callback.onSuccess(user);
            callback.onComplete();
        });
    }

    public static void fetchAllSubjects(Activity host, @NotNull FirebaseFirestore firestore, @NotNull AsyncCallback<List<Subject>> callback) {
        callback.onStart();
        firestore.collection(Constants.SUBJECTS)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(host, (queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getLocalizedMessage());
                        callback.onComplete();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Subject> subjects = queryDocumentSnapshots.toObjects(Subject.class);
                        callback.onSuccess(subjects);
                    } else {
                        callback.onError("Unable to load subjects");
                    }
                    callback.onComplete();
                });
    }

    public static void fetchAllFeedbacks(Activity host, @NotNull FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, @NotNull AsyncCallback<List<Feedback>> callback) {
        callback.onStart();
        firestore.collection(Constants.FEEDBACK)
                .whereEqualTo("parent", prefs.getKey())
                .addSnapshotListener(host, (queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getLocalizedMessage());
                        callback.onComplete();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Feedback> subjects = queryDocumentSnapshots.toObjects(Feedback.class);
                        callback.onSuccess(subjects);
                    } else {
                        callback.onError("Unable to load subjects");
                    }
                    callback.onComplete();
                });
    }

    public static void getFeedback(Activity host, @NotNull FirebaseFirestore firestore, String feedbackKey, @NotNull AsyncCallback<Feedback> callback) {
        callback.onStart();
        firestore.collection(Constants.FEEDBACK)
                .document(feedbackKey)
                .get()
                .addOnCompleteListener(host, task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        callback.onSuccess(Objects.requireNonNull(task.getResult()).toObject(Feedback.class));
                        callback.onComplete();
                    } else {
                        callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                        callback.onComplete();
                    }
                }).addOnFailureListener(host, e -> {
            callback.onError(e.getLocalizedMessage());
            callback.onComplete();
        });
    }

    public static void fetchAllSchedules(Activity host, @NotNull FirebaseFirestore firestore, String tutor, @NotNull AsyncCallback<List<Schedule>> callback) {
        callback.onStart();
        firestore.collection(Constants.SCHEDULES)
                .whereEqualTo("tutor", tutor)
                .addSnapshotListener(host, (queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getLocalizedMessage());
                        callback.onComplete();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Schedule> schedules = queryDocumentSnapshots.toObjects(Schedule.class);
                        callback.onSuccess(schedules);
                    } else {
                        callback.onError("Unable to load subjects");
                    }
                    callback.onComplete();
                });
    }

    public static void getAllClients(Activity host, @NotNull FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, @NotNull AsyncCallback<List<Parent>> callback) {
        callback.onStart();
        firestore.collection(String.format(Constants.CLIENTS, prefs.getKey())).orderBy("name", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(host, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Parent> parents = task.getResult().toObjects(Parent.class);
                        callback.onSuccess(parents);
                        callback.onComplete();
                    } else {
                        callback.onError(task.getException().getLocalizedMessage() == null ? "Could not load all clients for this tutor" : task.getException().getLocalizedMessage());
                        callback.onComplete();
                    }
                }).addOnFailureListener(host, e -> {
            callback.onError(e.getLocalizedMessage());
            callback.onComplete();
        });
    }

    public static void getAllTutors(Activity host, @NotNull FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, @NotNull AsyncCallback<List<Tutor>> callback) {
        callback.onStart();
        firestore.collection(Constants.TUTORS).orderBy("name", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(host, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Tutor> tutors = task.getResult().toObjects(Tutor.class);
                        callback.onSuccess(tutors);
                        callback.onComplete();
                    } else {
                        callback.onError(task.getException().getLocalizedMessage() == null ? "Could not load all tutors at this time" : task.getException().getLocalizedMessage());
                        callback.onComplete();
                    }
                }).addOnFailureListener(host, e -> {
            callback.onError(e.getLocalizedMessage());
            callback.onComplete();
        });
    }

    public static void searchFor(FirebaseFirestore firestore, String query, @NotNull AsyncCallback<List<? extends BaseDataModel>> callback) {
        callback.onStart();
        if (InputValidator.INSTANCE.hasValidInput(query)) {
            // Get all subjects
            Task<QuerySnapshot> subjectQueryTask = firestore.collection(Constants.SUBJECTS).get();

            // Get all tutors
            Task<QuerySnapshot> tutorQueryTask = firestore.collection(Constants.TUTORS).get();

            // Get all reports
            Task<QuerySnapshot> reportQueryTask = firestore.collection(Constants.REPORTS).get();
            Task<QuerySnapshot> complaintQueryTask = firestore.collection(Constants.COMPLAINTS).get();
            try {
                List<Subject> subjects = Tasks.await(subjectQueryTask).toObjects(Subject.class);
                List<Tutor> tutors = Tasks.await(tutorQueryTask).toObjects(Tutor.class);
                List<Report> reports = Tasks.await(reportQueryTask).toObjects(Report.class);
                List<Complaint> complaints = Tasks.await(complaintQueryTask).toObjects(Complaint.class);

                // Results
                List<BaseDataModel> response = new ArrayList<>(0);
                for (Subject subject : subjects) {
                    if (subject.getName().contains(query)) response.add(subject);
                }

                for (Tutor tutor : tutors) {
                    if (Objects.requireNonNull(tutor.getName()).contains(query) || Objects.requireNonNull(tutor.getEmail()).contains(query))
                        response.add(tutor);
                }

                for (Report report : reports) {
                    if (report.getWard().contains(query)) response.add(report);
                }

                for (Complaint complaint : complaints) {
                    if (complaint.getDescription().contains(query)) response.add(complaint);
                }

                callback.onSuccess(response);
                callback.onComplete();

            } catch (ExecutionException | InterruptedException | IllegalStateException e) {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            }
        } else {
            callback.onError("Please enter a valid input to query");
            callback.onComplete();
        }
    }

    public static void requestService(@NotNull FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, String tutor, @NotNull AsyncCallback<Void> callback) {
        callback.onStart();

        // Document reference created
        DocumentReference document = firestore.collection(Constants.REQUESTS).document();
        // Get key from document
        String key = document.getId();

        // Create new request data model
        Request request = new Request(key, prefs.getKey(), tutor, System.currentTimeMillis());


        document.set(request).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
                callback.onComplete();
            } else {
                callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                callback.onComplete();
            }
        }).addOnFailureListener(e -> {
            callback.onError(e.getLocalizedMessage());
            callback.onComplete();
        });
    }

    /**
     * Requests received by a Tutor
     *
     * @param host      calling activity
     * @param firestore Firebase firestore
     * @param prefs     User shared prefs. Contains user's login key and type
     * @param callback  Callback function after the process is carried out
     */
    public static void getReceivedRequests(Activity host, FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, @NotNull AsyncCallback<List<Request>> callback) {
        callback.onStart();

        if (prefs.isLoggedIn()) {
            firestore.collection(Constants.REQUESTS)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .whereEqualTo("tutor", prefs.getKey())
                    .addSnapshotListener(host, (queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            callback.onError(e.getLocalizedMessage());
                            callback.onComplete();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            callback.onSuccess(queryDocumentSnapshots.toObjects(Request.class));
                        } else {
                            callback.onError("unable to get received requests for this tutor");
                        }
                        callback.onComplete();

                    });
        } else {
            callback.onError("You need to be logged in first");
            callback.onComplete();
        }
    }

    /**
     * Requests sent by a Parent
     *
     * @param host      calling activity
     * @param firestore Firebase firestore
     * @param prefs     User shared prefs. Contains user's login key and type
     * @param callback  Callback function after the process is carried out
     */
    public static void getSentRequests(Activity host, FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, @Nullable String tutor, @NotNull AsyncCallback<Boolean> callback) {
        callback.onStart();
        if (prefs.isLoggedIn()) {

            // Get all requests sent by the current parent
            Query query = firestore.collection(Constants.REQUESTS)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .whereEqualTo("parent", prefs.getKey())
                    .whereEqualTo("tutor", tutor);

            // Get results for a particular tutor
            /*if (tutor != null && !TextUtils.isEmpty(tutor)) */

            query.get().addOnCompleteListener(host, task -> {
                if (task.isSuccessful()) {
                    ExtensionUtils.debugLog(host, Objects.requireNonNull(task.getResult()).toObjects(Request.class));
                    callback.onSuccess(Objects.requireNonNull(task.getResult()).toObjects(Request.class).isEmpty());
                    callback.onComplete();
                } else {
                    callback.onError(task.getException() != null ? task.getException().getLocalizedMessage() : "Could not load requests");
                }
            }).addOnFailureListener(host, e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });
        } else {
            callback.onError("You need to be logged in first");
            callback.onComplete();
        }
    }

    /**
     * Gets the current request by its ID
     */
    public static void getCurrentRequest(Activity host, @NotNull FirebaseFirestore firestore, String requestId, @NotNull AsyncCallback<Request> callback) {
        callback.onStart();
        firestore.collection(Constants.REQUESTS).document(requestId).get()
                .addOnCompleteListener(host, task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot != null && snapshot.exists()) {
                            Request request = snapshot.toObject(Request.class);
                            callback.onSuccess(request);
                            callback.onComplete();
                        }
                    } else {
                        callback.onError("Unable to retrieve the desired request. Please try again later");
                        callback.onComplete();
                    }
                }).addOnFailureListener(host, e -> {
            callback.onError(e.getLocalizedMessage());
            callback.onComplete();
        });

    }

    /**
     * Add new ward
     */
    public static void addWard(FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, WardCredentials credentials, @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        if (prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.PARENT)) {
            firestore.collection(Constants.PARENTS).document(prefs.getKey()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot == null) {
                                callback.onError("Could not current parent\'s information");
                                callback.onComplete();
                                return;
                            }

                            // Get parent's information
                            Parent parent = snapshot.toObject(Parent.class);

                            // Create a new document reference for the ward
                            DocumentReference document = firestore.collection(String.format(Constants.WARDS, prefs.getKey())).document();

                            // Create new Ward
                            Ward ward = new Ward(Objects.requireNonNull(parent).getEmail(), credentials.getName(), credentials.getAvatar(),
                                    document.getId(), parent.getToken(), credentials.getName(), BaseUser.Type.WARD);

                            // Push data to database
                            document.set(ward).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    // Add new ward credentials
                                    parent.getWards().add(document.getId());

                                    // Update parents list of wards
                                    firestore.collection(Constants.PARENTS).document(prefs.getKey())
                                            .set(parent)
                                            .addOnCompleteListener(task2 -> {
                                                callback.onSuccess(null);
                                                callback.onComplete();
                                            });
                                }
                            });
                        } else {
                            callback.onError("Could not get the current user");
                            callback.onComplete();
                        }
                    }).addOnFailureListener(e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });


        } else {
            callback.onError("Please login as a parent first to proceed");
            callback.onComplete();
        }
    }

    /**
     * Add new subject
     */
    public static void addSubject(FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, Subject subject, @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        if (prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.TUTOR)) {
            // Create a new document reference for the ward. We use the subject's key to avoid duplication of entries
            DocumentReference document = firestore.collection(String.format(Constants.TUTOR_SUBJECTS, prefs.getKey())).document(subject.getKey());

            // Push data to database
            document.set(subject).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess(null);
                    callback.onComplete();
                } else {
                    callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                    callback.onComplete();
                }
            }).addOnFailureListener(e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });


        } else {
            callback.onError("Please login as a parent first to proceed");
            callback.onComplete();
        }
    }

    public static void sendFeedback(FirebaseFirestore firestore, @NotNull UserSharedPreferences prefs, Feedback feedback, @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        if (prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.TUTOR)) {
            // Create a new document reference for the ward. We use the subject's key to avoid duplication of entries
            DocumentReference document = firestore.collection(Constants.FEEDBACK).document();
            feedback.setKey(document.getId());

            // Push data to database
            document.set(feedback).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess(null);
                    callback.onComplete();
                } else {
                    callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                    callback.onComplete();
                }
            }).addOnFailureListener(e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });


        } else {
            callback.onError("Please login as a parent first to proceed");
            callback.onComplete();
        }
    }

    public static void getSubject(FirebaseFirestore firestore, String key, AsyncCallback<Subject> callback) {
        callback.onStart();

        firestore.collection(Constants.SUBJECTS).document(key).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                callback.onSuccess(task.getResult().toObject(Subject.class));
                callback.onComplete();
            } else {
                callback.onError("Unable to get this subject");
                callback.onComplete();
            }
        }).addOnFailureListener(e -> {
            callback.onError(e.getLocalizedMessage());
            callback.onComplete();
        });
    }

    /**
     * Get all Subjects for this Tutor
     */
    public static void getTutorSubjects(Activity host, @NotNull FirebaseFirestore firestore, String tutor, @NotNull AsyncCallback<List<Subject>> callback) {
        callback.onStart();
        firestore.collection(String.format(Constants.TUTOR_SUBJECTS, tutor)).get()
                .addOnCompleteListener(host, task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            callback.onSuccess(snapshot.toObjects(Subject.class));
                        } else {
                            callback.onError("Cannot retrieve subjects at this time");
                        }
                        callback.onComplete();
                    } else {
                        callback.onError("Unable to retrieve subjects for this tutor");
                        callback.onComplete();
                    }
                }).addOnFailureListener(host, e -> {

        });

    }

    public static void toggleTutorRequest(FirebaseFirestore firestore,
                                          UserSharedPreferences prefs,
                                          Parent parent,
                                          boolean state,
                                          String requestId, AsyncCallback<Void> callback) {
        callback.onStart();
        if (prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.TUTOR)) {
            if (state) {
                DocumentReference document = firestore.collection(String.format(Constants.CLIENTS, prefs.getKey())).document(parent.getKey());
                document.set(parent).addOnFailureListener(e -> {
                    callback.onError(e.getLocalizedMessage());
                    callback.onComplete();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firestore.collection(Constants.REQUESTS).document(requestId).delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) callback.onSuccess(null);
                                    else callback.onError("Unable to save client");
                                    callback.onComplete();
                                });
                    } else {
                        callback.onError("Unable to save client");
                        callback.onComplete();
                    }
                });
            } else {
                firestore.collection(Constants.REQUESTS).document(requestId).delete()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) callback.onSuccess(null);
                            else callback.onError("Unable to delete request");
                            callback.onComplete();
                        });
            }
        } else {
            callback.onError("Unable to add parent to your clients");
            callback.onComplete();
        }
    }

    /**
     * Add new {@link Assignment}
     */
    public static void sendAssignment(FirebaseFirestore firestore,
                                      StorageReference storage,
                                      @NotNull UserSharedPreferences prefs,
                                      String comment,
                                      String filePath,
                                      @Nullable String ward,
                                      String subject,
                                      long startDate,
                                      long endDate,
                                      @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        if (prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.TUTOR)) {
            uploadFile(storage, Uri.parse(filePath), new AsyncCallback<String>() {
                @Override
                public void onError(@Nullable String error) {
                    callback.onError(error);
                    callback.onComplete();
                }

                @Override
                public void onSuccess(@Nullable String response) {
                    if (response == null) {
                        callback.onError("Could not upload assignment file");
                        callback.onComplete();
                        return;
                    }

                    DocumentReference document = firestore.collection(String.format(Constants.ASSIGNMENTS, prefs.getKey())).document();
                    Assignment assignment = new Assignment(document.getId(), ward, comment, response, subject, startDate, endDate);
                    ExtensionUtils.debugLog("Assignment sent as: ", assignment);
                    document.set(assignment).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);

                        } else {
                            callback.onError("Unable to upload assignment.\n" + Objects.requireNonNull(task.getException()).getLocalizedMessage());
                        }
                        callback.onComplete();
                    }).addOnFailureListener(e -> {
                        callback.onError(e.getLocalizedMessage());
                        callback.onComplete();
                    });
                }

                @Override
                public void onStart() {
                    callback.onStart();
                }

                @Override
                public void onComplete() {

                }
            });

        } else {
            callback.onError("Please sign in as a tutor first");
            callback.onComplete();
        }
    }

    /**
     * Get a list of all {@linkplain Assignment}s uploaded by this {@link Tutor}
     */
    public static void getAllAssignments(Activity host,
                                         FirebaseFirestore firestore,
                                         @NotNull UserSharedPreferences prefs,
                                         @Nullable String subject,
                                         @NotNull AsyncCallback<List<Assignment>> callback) {
        callback.onStart();

        if (!prefs.isLoggedIn() || !prefs.getType().equals(BaseUser.Type.TUTOR)) {
            callback.onError("Please sign in as a tutor first");
            callback.onComplete();
            return;
        }

        Query query;
        if (subject == null) {
            query = firestore.collection(String.format(Constants.ASSIGNMENTS, prefs.getKey()));
        } else
            query = firestore.collection(String.format(Constants.ASSIGNMENTS, prefs.getKey())).whereEqualTo("subject", subject);

        // Attach a live listener
        query.addSnapshotListener(host, (queryDocumentSnapshots, e) -> {
            if (e != null) {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
                return;
            }

            if (queryDocumentSnapshots != null) {
                callback.onSuccess(queryDocumentSnapshots.toObjects(Assignment.class));
            } else callback.onError("Could not get assignments for this tutor and subject");
            callback.onComplete();

        });
    }

    /**
     * Get a list of all {@linkplain Assignment}s uploaded by this {@link Tutor}
     */
    public static void getAllStudents(Activity host,
                                      FirebaseFirestore firestore,
                                      @NotNull UserSharedPreferences prefs,
                                      @Nullable String subject,
                                      @NotNull AsyncCallback<List<Student>> callback) {
        callback.onStart();

        if (!prefs.isLoggedIn() || !prefs.getType().equals(BaseUser.Type.TUTOR)) {
            callback.onError("Please sign in as a tutor first");
            callback.onComplete();
            return;
        }

        Query query;
        if (subject == null) {
            query = firestore.collection(String.format(Constants.STUDENTS, prefs.getKey()));
        } else
            query = firestore.collection(String.format(Constants.STUDENTS, prefs.getKey())).whereEqualTo("subject", subject);

        // Attach a live listener
        query.addSnapshotListener(host, (queryDocumentSnapshots, e) -> {
            if (e != null) {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
                return;
            }

            if (queryDocumentSnapshots != null) {
                callback.onSuccess(queryDocumentSnapshots.toObjects(Student.class));
            } else callback.onError("Could not get assignments for this tutor and subject");
            callback.onComplete();

        });
    }

    public static void getComplaints(@NotNull BaseActivity host, @NotNull AsyncCallback<List<Complaint>> callback) {
        callback.onStart();
        FirebaseFirestore firestore = host.firestore;
        UserSharedPreferences prefs = host.prefs;

        if (prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.TUTOR)) {
            firestore.collection(Constants.COMPLAINTS).whereEqualTo("tutor", prefs.getKey())
                    .get()
                    .addOnCompleteListener(host, task -> {
                        if (task.isSuccessful()) {
                            List<Complaint> complaints = Objects.requireNonNull(task.getResult()).toObjects(Complaint.class);
                            callback.onSuccess(complaints);
                            callback.onComplete();
                        } else {
                            callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                            callback.onComplete();
                        }
                    }).addOnFailureListener(host, e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });
        } else {
            callback.onError("Please sign in as a tutor first");
            callback.onComplete();
        }

    }

    public static void postComplaint(@NotNull BaseActivity host, String parent, String tutor, String description, @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        FirebaseFirestore firestore = host.firestore;
        UserSharedPreferences prefs = host.prefs;

        if (prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.PARENT)) {
            DocumentReference document = firestore.collection(Constants.COMPLAINTS).document();
            Complaint complaint = new Complaint(document.getId(), parent, tutor, description, System.currentTimeMillis());

            document.set(complaint)
                    .addOnCompleteListener(host, task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                            callback.onComplete();
                        } else {
                            callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                            callback.onComplete();
                        }
                    }).addOnFailureListener(host, e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });
        } else {
            callback.onError("Please sign in as a parent first");
            callback.onComplete();
        }

    }

    public static void postSchedule(@NotNull BaseActivity host,
                                    @NonNull String ward,
                                    String subject,
                                    long startDate,
                                    long endDate,
                                    @NotNull AsyncCallback<Void> callback) {
        callback.onStart();
        FirebaseFirestore firestore = host.firestore;
        UserSharedPreferences prefs = host.prefs;

        if (prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.TUTOR)) {
            DocumentReference document = firestore.collection(Constants.SCHEDULES).document();
            Schedule schedule = new Schedule(document.getId(), prefs.getKey(), subject, ward, startDate, endDate, false, new Date());

            document.set(schedule)
                    .addOnCompleteListener(host, task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                            callback.onComplete();
                        } else {
                            callback.onError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                            callback.onComplete();
                        }
                    }).addOnFailureListener(host, e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });
        } else {
            callback.onError("Please sign in as a tutor first");
            callback.onComplete();
        }

    }

    public static void getTimetableForUser(BaseActivity host, String ward, AsyncCallback<List<Timetable>> callback) {
        callback.onStart();
        FirebaseFirestore firestore = host.firestore;
        UserSharedPreferences prefs = host.prefs;

        if (prefs.getType().equals(BaseUser.Type.PARENT)) {
            firestore.collection(String.format(Constants.TIMETABLES, prefs.getKey(), ward))
                    .orderBy("time", Query.Direction.DESCENDING)
                    .addSnapshotListener(host, (queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            callback.onError(e.getLocalizedMessage());
                            callback.onComplete();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            List<Timetable> timetables = queryDocumentSnapshots.toObjects(Timetable.class);
                            callback.onSuccess(timetables);
                            callback.onComplete();
                        } else {
                            callback.onError("Unable to get timetables for ward");
                            callback.onComplete();
                        }

                    });

        } else {
            callback.onError("Sign in as a parent first");
            callback.onComplete();
        }

    }

    public static void getWard(@NotNull BaseActivity host, String ward, @NotNull AsyncCallback<Ward> callback) {
        callback.onStart();

        if (host.prefs.getType().equals(BaseUser.Type.PARENT)) {
            host.firestore.collection(String.format(Constants.WARDS, host.prefs.getKey())).document(ward)
                    .get()
                    .addOnCompleteListener(host, task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            Ward parentWard = task.getResult().toObject(Ward.class);
                            callback.onSuccess(parentWard);
                            callback.onComplete();
                        } else {
                            callback.onError("Unable to fetch ward\'s information");
                            callback.onComplete();
                        }
                    }).addOnFailureListener(host, e -> {
                callback.onError(e.getLocalizedMessage());
                callback.onComplete();
            });
        } else {
            callback.onError("Sign in as a parent first");
            callback.onComplete();
        }
    }
}
