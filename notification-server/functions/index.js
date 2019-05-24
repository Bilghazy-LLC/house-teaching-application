// Add packages
const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin
admin.initializeApp();

// Store user information in the database
// exports.alertNewUser = functions.auth.user().onCreate((record, context) => {
//     // get the database instance
//     let db = admin.firestore();

//     // Get default profile image
//     var defaultAvatar = 'https://firebasestorage.googleapis.com/v0/b/house-teaching.appspot.com/o/default-avatar.png?alt=media&token=b1ab3898-f830-42b6-aa08-18c70627bb87';

//     // get the database path for users & we store the user's information there
//     return db.collection('users').doc(record.uid).set({
//         name: record.displayName ? record.displayName : 'No username',
//         email: record.email ? record.email : 'No email',
//         phone: record.phoneNumber ? record.phoneNumber : 'No phone number',
//         uid: record.uid,
//         token: null,
//         avatar: record.photoURL ? record.photoURL.toString() : defaultAvatar
//     }).then(() => {
//         return console.log(`User created as: ${record.email} & UID: ${record.uid}`);
//     }).catch(err => {
//         return console.log(`Could not create user document. ${err.message}`);
//     })
// });

// Send notification to the user's device
exports.sendNotification = functions.firestore.document('users/{uid}').onUpdate((change, context) => {
    // Extract the user's ID
    var uid = context.params.uid;

    // Compare the user's token
    var oldToken = change.before.data().token;

    if (change.after.exists && change.after.data().token === oldToken) {
        return console.log('User token has not changed');
    } else {
        var newToken = change.after.data().token;
        if (newToken) {
            // Notification body
            var payload = {
                data: {
                    title: 'New token found',
                    body: 'Your device token has been updated successfully',
                    token: newToken.toString(),
                    uid: uid
                }
            };

            // Send a notification to the user
            return admin.messaging().sendToDevice(newToken, payload).then(() => {
                return console.log('Notification sent to the device.', newToken);
            }).catch(err => {
                if (err) {
                    return console.log(err.message);
                } else return null;
            });
        } else {
            return console.log('Cannot send a notification to a null token');
        }
    }
});

exports.sendRequest = functions.firestore.document('requests/{requestId}').onCreate((change, context) => {
    // Extract the user's ID
    var requestId = context.params.requestId ? context.params.requestId : change.id;
    var data = change.data();

    // Get the parent and the tutor
    var parent = data.parent;
    var tutor = data.tutor;

    // Get the tutor's device token and send a notification to their device
    admin.firestore().collection('tutors').doc(tutor).get()
        .then(snapshot => {
            if (snapshot.exists) {
                var deviceToken = snapshot.data().token;

                // Get parent's information
                admin.firestore().collection('parents').doc(parent)
                    .get().then(querySnapshot => {
                        if (querySnapshot.exists) {
                            var parentData = querySnapshot.data();

                            // Send notification
                            admin.messaging().sendToDevice(deviceToken, {
                                data: {
                                    id: requestId,
                                    parent: parent,
                                    title: `Request received from ${parentData.name}`,
                                    message: 'Tap on this to view more details',
                                    createdAt: `${new Date().getTime()}`,
                                    updatedAt: `${data.timestamp}`,
                                    type: 'tutor-request'
                                }
                            }).then(response => {
                                return console.log('Notification sent to tutor successfully');
                            }).catch(err => {
                                if (err) {
                                    return console.log(err);
                                }
                            })

                        } else {
                            return console.log("Unable to get the parent.", parent);
                        }
                    }).catch(error => {
                        if (error) {
                            return console.log(error);
                        }
                    })

            } else {
                return console.log("Unable to get the tutor.", tutor);

            }
        }).catch(error => {
            if (error) {
                return console.log(error);
            }
        });
});

exports.notifyRequest = functions.firestore.document('requests/{requestId}').onWrite((change, context) => {
    // Extract the user's ID
    var requestId = context.params.requestId ? context.params.requestId : change.id;
    var data = change.before.data();

    // Get the parent and the tutor
    var parent = data.parent;
    var tutor = data.tutor;

    // Get the parent's device token and send a notification to their device
    // Get parent's information
    admin.firestore().collection('parents').doc(parent)
        .get().then(querySnapshot => {
            if (querySnapshot.exists) {
                var parentData = querySnapshot.data();
                var deviceToken = parentData.token;

                admin.firestore().collection('tutors').doc(tutor)
                    .get()
                    .then(snapshot => {

                        if (snapshot.exists) {
                            // Send notification
                            admin.messaging().sendToDevice(deviceToken, {
                                data: {
                                    id: requestId,
                                    tutor: snapshot.data().key,
                                    title: `Service request update`,
                                    message: 'Your request for service was accepted',
                                    createdAt: `${new Date().getTime()}`,
                                    updatedAt: `${data.timestamp}`,
                                    type: 'tutor-request-status'
                                }
                            }).then(response => {
                                return console.log('Notification sent to tutor successfully');
                            }).catch(err => {
                                if (err) {
                                    return console.log(err);
                                }
                            })
                        } else {
                            return console.log("Unable to get this tutor.", tutor);

                        }
                    }).catch(err => {
                        if (err) {
                            return console.log(err);
                        }
                    })

            } else {
                return console.log("Unable to get the parent.", parent);
            }
        }).catch(error => {
            if (error) {
                return console.log(error);
            }
        })
});

// 
exports.wardAssignment = functions.firestore.document('tutors/{tutorId}/assignments/{assignmentId}').onCreate((change, context) => {
    // Get params from the context
    var assignmentId = context.params.assignmentId;
    var tutorId = context.params.tutorId;

    var parent = change.data().ward;
    console.log(`Parent UID: ${parent}`);
    
    if (parent) {
        return admin.firestore().doc(`parents/${parent}`).get().then(snapshot => {
            console.log(snapshot.data());
            
            if (snapshot.exists) {
                var parentInfo = snapshot.data();

                // Get the token field
                var deviceToken = parentInfo.token;
                if (deviceToken) {
                    return admin.messaging().sendToDevice(deviceToken, {
                        data: {
                            title: `New Assignment`,
                            message: change.data().comment,
                            tutor: tutorId,
                            key: assignmentId,
                            token: deviceToken,
                            type: 'ward-assignment'
                        }
                    }).then(() => {
                        return console.log('Notification sent to parent successfully');
                    }).catch(err => {
                        if (err) {
                            return console.log(err.message);
                        }
                    })
                } else {
                    return console.log(`This parent has no valid device token yet: ${parent}`);

                }
            } else {
                return console.log('Could not find the parent with ID: ', parent);

            }
        }).catch(err => {
            if (err) {
                return console.log(err.message);
            }
        });
    } else {
        return console.log("unable to retreive parent's information");
    }

});

// Send Feedback from tutor to parent
exports.sendFeedback = functions.firestore.document('feedback/{key}').onCreate((change, context) => {
    // Extract the key
    var key = context.params.key;

    var parent = change.data().parent;
    console.log(`Parent's UID: ${parent}`);

    // get the parent's device token
    return admin.firestore().doc(`parents/${parent}`).get().then(snapshot => {
        if (snapshot.exists) {
            var parentInfo = snapshot.data();

            // get the token field
            var deviceToken = parentInfo.token;
            if (deviceToken) {
                return admin.messaging().sendToDevice(deviceToken, {
                    data: {
                        title: `Feedback on your ward received`,
                        message: change.data().message,
                        tutor: change.data().tutor,
                        key: change.id,
                        token: deviceToken,
                        type: 'tutor-feedback'
                    }
                }).then(() => {
                    return console.log('Notification sent to parent successfully');
                }).catch(err => {
                    if (err) {
                        return console.log(err.message);
                    }
                })
            } else {
                return console.log(`This parent has no valid device token yet: ${parent}`);

            }
        } else {
            return console.log('Could not find the parent with ID: ', parent);

        }
    }).catch(err => {
        if (err) {
            return console.log(err.message);
        }
    });
});