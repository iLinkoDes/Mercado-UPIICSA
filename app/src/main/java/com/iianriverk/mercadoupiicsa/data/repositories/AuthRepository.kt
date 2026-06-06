package com.iianriverk.mercadoupiicsa.data.repositories

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class AuthRepository{
    private val auth : FirebaseAuth = Firebase.auth
    private val firestore : FirebaseFirestore = Firebase.firestore
}