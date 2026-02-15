# Gestion Tournois FFTT

## Présentation

Gestion Tournois FFTT est une application desktop développée en Java visant à gérer l’organisation complète de tournois de tennis de table selon les règles de la FFTT.

Le projet est conçu avec une approche orientée domaine (DDD) afin de séparer clairement la logique métier, l’interface utilisateur et l’infrastructure technique.

L’objectif final est de disposer d’un logiciel utilisable en conditions réelles de tournoi, capable de fonctionner hors ligne pendant la compétition, avec synchronisation ultérieure vers un serveur.

---

## Objectifs du projet

Le projet vise à permettre :

- la gestion des inscriptions joueurs selon les règles FFTT
- la création et la configuration de tournois et tableaux
- la gestion des capacités et des règles spécifiques (féminines, quotas, paiements)
- l’utilisation du logiciel en mode hors ligne le jour du tournoi
- la synchronisation des résultats vers un serveur après l’événement

L’architecture est pensée pour évoluer vers un backend serveur tout en conservant un cœur métier indépendant.

---

## Architecture technique

Langage principal : Java  
Interface utilisateur : JavaFX  
Base de données locale : SQLite  
Build : Maven  

Organisation du code :

- domain : cœur métier et règles FFTT
- ui/javafx : interface utilisateur desktop
- infra : accès base de données et repositories
- common : exceptions et éléments transverses

Le cœur métier est totalement indépendant de l’UI et de la base de données.

---

## Fonctionnalités réalisées

### Cœur métier

Le domaine métier est largement implémenté et testé.

Modélisation :

- Player (licence, club, points, certificat médical, sexe)
- Tournament
- Tableau
- Registration

Règles métier implémentées :

- nombre maximum de tableaux par jour
- nombre maximum total
- gestion des capacités des tableaux
- règles spécifiques féminines :
  - NONE
  - ANY_TABLEAU
  - SPECIFIC_TABLEAU_CODE
- validation multi-tableaux
- gestion des paiements :
  - paiement sur place avec confirmation immédiate
  - paiement en ligne avec réservation et expiration
- suppression automatique des réservations expirées
- calcul du récapitulatif et du prix

Services métier :

- MultiTableauRegistrationService
- RegistrationCheckoutService
- RegistrationService

Tests unitaires couvrant :

- règles féminines
- quotas
- gestion des réservations
- expiration
- capacité après expiration

---

### Application desktop (JavaFX)

Fonctionnalités disponibles :

- écran d’accueil
- connexion organisme
- inscription organisme
- stockage sécurisé des comptes avec hash du mot de passe
- dashboard organisme
- affichage dynamique selon la présence d’un tournoi courant
- badge de statut du tournoi :
  - DRAFT
  - OPEN
  - RUNNING
  - FINISHED
- affichage des informations du tournoi
- affichage de la liste des tableaux
- activation des actions selon le statut du tournoi

---

### Base de données locale

Base SQLite locale utilisée pour :

- comptes organismes
- état de l’application
- tournois
- tableaux

Le schéma est géré via un fichier SQL versionné.

Le logiciel fonctionne entièrement en local.

---

## Fonctionnalités prévues

### Court terme

- création d’un tournoi depuis l’interface
- modification des informations du tournoi
- gestion du statut du tournoi
- gestion complète des tableaux depuis l’UI
- rafraîchissement dynamique du dashboard

### Moyen terme

- gestion des joueurs
- inscriptions aux tableaux depuis l’interface
- gestion des listes d’inscrits
- gestion des paiements côté interface
- gestion des résultats

### Long terme

- backend serveur (Spring Boot)
- synchronisation des données
- gestion multi-tournois
- export des résultats
- interface joueur complète
- déploiement utilisable en production

---

## Philosophie du projet

Le projet suit plusieurs principes :

- séparation stricte du domaine métier
- logique métier indépendante de l’infrastructure
- architecture évolutive vers un mode client-serveur
- fonctionnement offline-first
- code lisible et maintenable

---

## État actuel

Le cœur métier est stable et fortement testé.

L’application desktop est fonctionnelle pour la gestion des comptes organismes et l’affichage du dashboard avec lecture de la base SQLite.

Le projet est prêt pour l’implémentation des écrans de création et de gestion de tournoi.

---

## Lancer le projet

Prérequis :

- Java JDK 20 ou supérieur
- Maven

Commande :

mvn clean javafx:run


La base de données locale sera créée automatiquement si elle n’existe pas.

---

## Auteur

Projet personnel visant à développer un logiciel complet de gestion de tournois FFTT avec une architecture robuste et évolutive.
