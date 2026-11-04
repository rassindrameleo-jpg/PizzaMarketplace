# PizzaMarketplace

## US5 - Notifications du suivi de commande

Pour cette user story, nous avons choisi le pattern **Observer**. L'idée est simple : quand le statut d'une commande change, les différents moyens de notification doivent être prévenus automatiquement.

La classe `Order` joue le rôle de sujet. Elle garde la liste des listeners enregistrés et les informe lorsqu'un nouveau statut est appliqué. Les listeners représentent les canaux de notification, par exemple l'email ou le SMS. Pour le moment, chaque listener affiche simplement un message dans la console afin de simuler l'envoi d'une notification.

Ce choix évite de mettre toute la logique de notification directement dans `Order`. On peut donc ajouter un nouveau moyen de notification sans modifier cette classe. Les listeners peuvent aussi être activés ou désactivés à tout moment avec `addStatusListener` et `removeStatusListener`, ce qui correspond aux besoins de l'US5.

## US6 - Annuler les modifications du panier

Pour cette user story, nous avons choisi le pattern **Memento**. Le panier doit pouvoir revenir à un état précédent sans que son contenu interne soit exposé ou géré directement par l'utilisateur.

Avant chaque modification, `Cart` sauvegarde une copie de son état dans l'historique. La méthode `undo` restaure le dernier état sauvegardé, tandis que `redo` permet de réappliquer une modification annulée. Lorsqu'une nouvelle modification est faite après un `undo`, l'historique `redo` est vidé, comme dans un panier classique.

L'historique est limité aux 10 dernières modifications afin de garder un fonctionnement simple et d'éviter de conserver inutilement trop d'états. Ce pattern permet de séparer la gestion de l'historique de la logique principale du panier.