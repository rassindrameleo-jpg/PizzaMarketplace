# PizzaMarketplace

API de commande de pizzas pour une marketplace qui regroupe trois chaînes : **Da Michele**, **Rossopomodoro** et **Pizzium**.
Le client compose son panier, la commande est validée, payée puis suivie jusqu'à la livraison.

Le projet a été réalisé dans le cadre du cours **Design Patterns**. L'objectif principal est l'utilisation justifiée de patterns de conception et un code découplé, plutôt qu'une application complète (pas d'interface graphique ni de base de données).

## Prérequis techniques

- Java 21
- Maven 3.8+
- IntelliJ IDEA (recommandé)

## Installation et lancement

```bash
git clone https://github.com/rassindrameleo-jpg/PizzaMarketplace.git
cd PizzaMarketplace
mvn clean compile
```

La démonstration se lance depuis la classe `Main` (bouton ▶ dans IntelliJ), ou en ligne de commande :

```bash
java -cp target/classes Main
```

## Tests

```bash
mvn clean test
```

## Architecture

Le projet suit une **architecture hexagonale** (ports & adapters). Les dépendances vont uniquement vers l'intérieur : le domaine ne dépend de rien.

```
src/main/java/fr/pizzeriamarketplace
├── domain/          # Modèle métier et règles métier (Pizza, Cart, Order, stratégies de prix…)
├── application/     # Cas d'usage et ports (interfaces vers l'extérieur)
│   ├── port/out/    # ex. OrderRepository
│   └── usecase/     # ex. GetOrderHistory
└── infrastructure/  # Adaptateurs techniques (ex. stockage en mémoire)
```

Choix communs à toute l'équipe :
- **Montants en centimes (`int`)** pour éviter les erreurs d'arrondi des nombres à virgule. La conversion en euros se fait uniquement à l'affichage.
- **Pas de base de données** : les commandes sont stockées en mémoire derrière le port `OrderRepository`. On pourrait brancher une vraie base sans modifier le domaine ni les cas d'usage.
- **Polymorphisme plutôt que `switch` / `if`** sur le type, pour respecter le principe ouvert/fermé.

## US1 - Chaînes, menus et prix par taille

Pour cette user story, nous avons combiné deux patterns : **Strategy** pour la règle de prix selon la taille, et **Abstract Factory** pour les enseignes.

Chaque enseigne calcule le prix d'une taille à sa façon : Da Michele ajoute un montant fixe (S -2 €, L +3 €, XL +5 €), Rossopomodoro applique un multiplicateur (S ×0.8, L ×1.3, XL ×1.5) et Pizzium ne propose que la taille M. L'interface `SizePricingStrategy` définit deux méthodes : `supports(size)`, qui indique si une taille est proposée, et `priceFor(mediumPriceCents, size)`, qui calcule le prix à partir du prix de référence en taille M. Elle a trois implémentations :
- `FixedSurchargeSizePricing` : ajoute un supplément par taille, sans jamais descendre en dessous de zéro ;
- `MultiplierSizePricing` : applique un pourcentage par taille (80 pour ×0.8), arrondi au centime. Les pourcentages sont des entiers pour rester en centimes sans nombre à virgule ;
- `SingleSizePricing` : ne propose qu'une seule taille et refuse les autres.

Comme pour les réductions de l'US2, nous avons fait une classe par **formule de calcul**, et non une classe par enseigne. Les valeurs (−200, 130 %…) sont passées au constructeur : une nouvelle enseigne qui utilise des suppléments réutilise `FixedSurchargeSizePricing` avec ses propres montants. Les tailles proposées sont simplement celles présentes dans la map, et toute taille non proposée est refusée avec une erreur claire.

Pour les enseignes, nous avons choisi l'**Abstract Factory**. L'interface `PizzaChain` crée une famille d'objets liés entre eux : le menu de l'enseigne (`createMenu`) et sa règle de taille (`createSizePricing`). Le Factory Method ne crée qu'un seul produit ; ici chaque enseigne en fournit plusieurs qui doivent rester cohérents. Ce choix garantit qu'on ne peut pas associer le menu de Pizzium à la grille de prix de Da Michele. Chaque enseigne (`DaMichele`, `Rossopomodoro`, `Pizzium`) regroupe toute sa configuration dans une seule classe : **ajouter une enseigne revient à ajouter une classe**, comme le demande l'US1.

Les enseignes sont regroupées dans `ChainCatalog`, qui les reçoit dans son constructeur depuis `Main` et permet de les retrouver par leur nom. Le catalogue ne connaît que l'interface `PizzaChain` : il ne change jamais quand une enseigne est ajoutée. Un test le vérifie avec une enseigne écrite uniquement pour le test.

Enfin, le client choisit une enseigne avant de composer sa commande : `Cart` et `Order` sont donc rattachés à une `PizzaChain`. Chaque `Pizza` porte seulement son prix en taille M, et c'est la règle de l'enseigne qui calcule le prix final. `subtotalCents()` donne le total avant réduction, sur lequel s'appliquent ensuite les stratégies de l'US2. Le panier refuse aussi une taille que l'enseigne ne propose pas, par exemple une pizza L chez Pizzium.

## US2 - Réductions

Pour cette user story, nous avons choisi le pattern **Strategy**. Le prix d'une commande peut être calculé de plusieurs façons : sans réduction, avec un pourcentage (happy hour, fidélité) ou avec un montant fixe (code promo). Chaque façon de calculer est une stratégie séparée.

L'interface `PricingStrategy` définit une seule méthode, `int apply(int subtotalCents)`, qui reçoit le sous-total en centimes et renvoie le prix après réduction. Elle a trois implémentations :
- `StandardPricing` : aucune réduction, le prix reste le même. Elle évite de devoir gérer un cas « pas de réduction » avec un `null` ou un `if` ;
- `PercentageDiscountPricing` : applique un pourcentage de réduction, arrondi au centime ;
- `FixedAmountDiscountPricing` : retire un montant fixe, sans jamais descendre en dessous de zéro.

Nous avons choisi une classe par **formule de calcul**, et non une classe par promotion. La happy hour (-20 %) et la fidélité (-10 %) utilisent la même formule : ce sont deux instances de `PercentageDiscountPricing` avec un pourcentage différent, passé au constructeur. Cela évite de dupliquer le code, et les valeurs métier sont définies à un seul endroit, là où les stratégies sont créées. Les valeurs invalides (pourcentage hors de 0 à 100, montant négatif) sont refusées dès la création de l'objet.

Les réductions ne se cumulent pas : une seule stratégie est appliquée par commande. Le choix de la stratégie (selon l'heure, le client ou un code promo) n'est pas fait par les stratégies elles-mêmes, mais par le code qui les utilise. Chaque stratégie ne s'occupe donc que du calcul. Le code qui calcule la commande ne connaît que l'interface : pour ajouter une nouvelle réduction, il suffit de créer une nouvelle classe, sans modifier le code existant.

## US3 - Validation de la commande
User Story : En tant que client, je souhaite que ma commande soit vérifiée avant sa validation afin de m'assurer qu'elle respecte les règles métier.
Design Pattern : Chain of Responsibility

j'ai choisi le pattern Chain of Responsibility pour organiser les différentes règles de validation sous forme d'une chaîne de validateurs indépendants.
Chaque validateur vérifie une condition et transmet la commande au suivant si elle est respectée.

Les validations implémentées sont :

- `NonEmptyCartValidator` : vérifie que le panier n'est pas vide.
- `MinimumAmountValidator ` : vérifie que le montant minimum de 15 € est atteint.
- `DeliveryZoneValidator` : vérifie que l'adresse se trouve dans une zone autorisée.
- `StoreOpenValidator` : vérifie que le restaurant est ouvert.
- `StockValidator` : vérifie la disponibilité des ingrédients.

Pourquoi ce pattern ?

Séparer les règles métier dans des classes indépendantes.
Arrêter immédiatement la validation lorsqu'une règle échoue.
Ajouter ou modifier des validations sans modifier les autres validateurs.



## US4 - Paiement

- User Story : En tant que client, je souhaite payer avec ma méthode préférée afin de finaliser ma commande.
- Design Pattern : Adapter

j'ai  choisi le pattern Adapter pour intégrer plusieurs systèmes de paiement simulés ayant des interfaces différentes.

Trois moyens de paiement sont proposés :

`Stripe `: makePayment(int cents)
`PayPal `: sendPayment(int amount, String currency)
`Apple Pay` : processTransaction(int amount, String currency)

Chaque système possède son propre Adapter (`StripeAdapter`, `PaypalAdapter`, `ApplePayAdapter`) qui implémente l'interface commune PaymentGateway.
Le `PaymentService` utilise cette interface sans dépendre directement des systèmes de paiement.
- `Pourquoi ce pattern ?`
Uniformiser l'utilisation de systèmes ayant des signatures différentes.
Permettre de changer de fournisseur sans modifier les systèmes de paiement.
Faciliter l'ajout de nouveaux moyens de paiement.

- `Gestion des échecs`
Le PaymentService utilise un moyen de paiement principal et un moyen de secours.
Si le premier paiement échoue (retourne false), le service tente automatiquement le second.

- `Exemple :`
Stripe indisponible → Basculement vers PayPal → Paiement simulé réussi
Les paiements sont simulés avec System.out.println() : aucune transaction réelle n'est effectuée.

## US5 - Notifications du suivi de commande

Pour cette user story, nous avons choisi le pattern **Observer**. L'idée est simple : quand le statut d'une commande change, les différents moyens de notification doivent être prévenus automatiquement.

La classe `Order` joue le rôle de sujet. Elle garde la liste des listeners enregistrés et les informe lorsqu'un nouveau statut est appliqué. Les listeners représentent les canaux de notification, par exemple l'email ou le SMS. Pour le moment, chaque listener affiche simplement un message dans la console afin de simuler l'envoi d'une notification.

Ce choix évite de mettre toute la logique de notification directement dans `Order`. On peut donc ajouter un nouveau moyen de notification sans modifier cette classe. Les listeners peuvent aussi être activés ou désactivés à tout moment avec `addStatusListener` et `removeStatusListener`, ce qui correspond aux besoins de l'US5.

## US6 - Annuler les modifications du panier

Pour cette user story, nous avons choisi le pattern **Memento**. Le panier doit pouvoir revenir à un état précédent sans que son contenu interne soit exposé ou géré directement par l'utilisateur.

Avant chaque modification, `Cart` sauvegarde une copie de son état dans l'historique. La méthode `undo` restaure le dernier état sauvegardé, tandis que `redo` permet de réappliquer une modification annulée. Lorsqu'une nouvelle modification est faite après un `undo`, l'historique `redo` est vidé, comme dans un panier classique.

L'historique est limité aux 10 dernières modifications afin de garder un fonctionnement simple et d'éviter de conserver inutilement trop d'états. Ce pattern permet de séparer la gestion de l'historique de la logique principale du panier.

## US7 - Tests des stratégies de réduction

Chaque stratégie de réduction a sa propre classe de test JUnit 5, dans `src/test/java/fr/pizzeriamarketplace/domain/pricing/` :
- `StandardPricingTest` : le prix reste inchangé ;
- `PercentageDiscountPricingTest` : calcul du pourcentage, arrondi au centime (999 centimes à -20 % donnent 799), cas limites 0 % et 100 %, refus d'un pourcentage hors de 0 à 100 ;
- `FixedAmountDiscountPricingTest` : soustraction du montant, prix jamais négatif, refus d'un montant négatif.

Comme les stratégies ne dépendent de rien d'autre qu'un nombre, elles se testent facilement : on crée la stratégie, on appelle `apply` et on compare le résultat. Les tests suivent la structure *given / when / then*.

## US8 - Pizza personnalisée

*À compléter.*

## US9 - Historique des commandes

Pour cette user story, nous avons utilisé le pattern **Repository**, déjà en place pour le stockage des commandes. Le client doit pouvoir consulter l'historique de ses commandes, trié par date, et le filtrer par statut.

Pour cela, la commande contient maintenant l'identifiant du client (`customerId`) et sa date de création (`createdAt`). Le port `OrderRepository` propose une nouvelle requête, `findByCustomerId`, implémentée dans `InMemoryOrderRepository`. Retrouver les commandes d'un client est une question d'accès aux données, comme un `WHERE` en SQL : elle a donc sa place dans le repository.

Le tri et le filtre par statut sont faits dans le cas d'usage `GetOrderHistory`, dans la couche `application`. Il propose deux méthodes : `forCustomer(customerId)` pour toutes les commandes du client, et `forCustomer(customerId, status)` pour celles d'un statut donné. Dans les deux cas, les commandes les plus récentes apparaissent en premier.

`GetOrderHistory` dépend uniquement de l'interface `OrderRepository`, pas de son implémentation en mémoire. On peut donc remplacer le stockage par une vraie base de données sans modifier le cas d'usage. Les tests utilisent directement `InMemoryOrderRepository`, ce qui évite d'écrire des faux objets.