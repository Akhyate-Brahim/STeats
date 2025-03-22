# SopiaTech Eats-Team-23-24


## Les principaux UC traités

#### [#19 Place an order](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/19)
As a Campus User,
I want to make a selection from a chosen restaurant's menu and finalize my order,
So that the restaurant can prepare and deliver my chosen menus.

#### [#6 Place a Group Order](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/6)


As a Campus User,
I want to have the ability to create or join group orders,
So that I can conveniently engage in collective ordering activities with other campus users.

#### [#8  Manage Restaurants](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/8)

As a campus Admin, I want to be able to add, delete and update restaurants informations so that the restaurants in our application stay up to date

#### [#26 Dynamic Pricing Based on User Type](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/26)
As a restaurant, I want to apply different prices for meals based on the user type so that I can offer special pricing to different groups of customers

#### [#25 Discount and Credit System for Orders with Threshold Logic](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/25)
As a campus user, I want to have a reduction for big orders so that iI pay less

#### [#34 Send notification when the delivery is ready](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/34)

As a campus user or a delivery person, I want to be notified about the delivery so that i have informations about it

#### [#27 Get statistical insights](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/27)
As a user, I want to get statistical insights so that can analyze data


### Extensions :
#### [#37 place a buffet order](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/37)
As a Campus User, I want to choose and order a buffet menu from a restaurant on campus, so that I can have it delivered to a specific location at a designated time for group events.
#### [#22 Customize menu choices for campus users](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/22)
As a campus user, I want to modify some menu items so that the menu fit my preferences
#### [#15 Rate and View Restaurant or DeliveryPerson Ratings](https://github.com/PNS-Conception/ste-23-24-equipe-r/issues/15)
As a CampusUser, I want to rate a restaurant or a delivery person so that can provide feedback to help improve the overall dining and delivery experience
## Comment lancer le projet et l'installer

### 1. Clonage du Projet :

```
git clone https://github.com/PNS-Conception/ste-23-24-equipe-r.git
```

### 2. Exécution des Tests :
```
mvn test
```

Cette commande lancera tous les tests du projet. Assurez-vous que Maven est installé sur votre machine.

### 3. Autres Commandes Utiles :

Pour nettoyer le projet (supprimer les fichiers générés lors de la compilation et des tests) :

```
mvn clean
```

Pour compiler le projet sans exécuter les tests :

```
mvn compile
```


Pour installer les dépendances et compiler le projet :

```
mvn install
```

### 4. Structure du projet : 

Le projet adopte une structure d'architecture en couches (*layered architecture*), s'alignant avec les principes de la conception pilotée par le domaine (Domain-Driven Design). Chaque couche, telle que les composants, regroupe en sous-ensembles les classes en fonction de leur contexte dans le domaine métier. Par exemple, au sein de la couche des composants, tous les composants gérant les commandes sont regroupés ensemble. Cette logique de classification est répliquée de manière similaire pour d'autres concepts du domaine, comme les restaurants, les utilisateurs, etc., afin d'assurer une organisation claire et de maintenir une cohérence au sein de chaque couche.

- components: Services et logique métier, groupés par fonctionnalité.
- connectors: Gestion de la communication avec systèmes externes.
- entities: Entités du domaine représentant concepts et règles métier.
- interfaces: Contrats pour les composants.
- repositories: pour abstraction de la persistance des données.
- util: Fonctions utilitaires.
   

   
