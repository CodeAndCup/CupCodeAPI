# CupCodeAPI

---
Warning: This project is in early development and may not be fully functional yet. Thanks for your understanding!
---

CupCodeAPI est une API Spigot moderne conçue pour faciliter la création d'interfaces utilisateurs avancées et d'interactions personnalisées dans Minecraft.  
Elle fournit des outils puissants pour manipuler les entités `TextDisplay`, créer des boutons interactifs, gérer les effets de survol (hover), mais aussi bien plus :

- **Utilitaires variés** pour simplifier le développement de plugins (gestion du chat, couleurs, etc.).
- **Système de commandes simple** pour créer rapidement des commandes personnalisées.
- **MenuAPI** pour concevoir facilement des menus interactifs et dynamiques.

## Fonctionnalités principales

- **Builders fluents** pour créer et configurer facilement des `TextDisplay` et boutons interactifs.
- **Gestion avancée du hover** : changez le texte, la couleur ou la taille lors du survol.
- **API d'interaction** : détectez et gérez les clics sur les entités d'affichage.
- **Utilitaires pratiques** pour le chat, les couleurs, la gestion des joueurs, etc.
- **Système de commandes simplifié** pour des commandes Bukkit/Spigot plus rapides à écrire.
- **MenuAPI** pour des interfaces graphiques Minecraft intuitives.
- **Extensible** : pensée pour être intégrée dans vos plugins Spigot/Bukkit.

## Installation

CupCodeAPI est disponible via Maven.  
Ajoutez le dépôt GitHub Packages et la dépendance suivante à votre `pom.xml` :

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/CodeAndCup/CupCodeAPI</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>fr.perrier</groupId>
        <artifactId>cupcodeapi</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```
Remplacez `VERSION` par la version souhaitée.  
N'oubliez pas de configurer vos identifiants GitHub pour l'accès au repository privé si nécessaire.

## Documentation

La documentation complète, les exemples d’utilisation et les guides d’intégration sont disponibles sur le [Wiki du projet](https://docs.cupcode.fr).

## Support

Pour toute question ou suggestion, ouvrez une issue sur le dépôt GitHub ou rejoignez notre Discord.

---

# English

CupCodeAPI is a modern Spigot API designed to make it easy to create advanced user interfaces and custom interactions in Minecraft.  
It provides powerful tools to manipulate `TextDisplay` entities, create interactive buttons, manage hover effects, and much more:

- **Various utilities** to simplify plugin development (chat management, colors, etc.).
- **Simple command system** to quickly create custom commands.
- **MenuAPI** to easily design interactive and dynamic menus.

## Main Features

- **Fluent builders** to easily create and configure `TextDisplay` and interactive buttons.
- **Advanced hover management**: change text, color, or scale on hover.
- **Interaction API**: detect and handle clicks on display entities.
- **Handy utilities** for chat, colors, player management, and more.
- **Simplified command system** for faster Bukkit/Spigot command creation.
- **MenuAPI** for intuitive Minecraft GUI interfaces.
- **Extensible**: designed to be integrated into your Spigot/Bukkit plugins.

## Installation

CupCodeAPI is available via Maven.  
Add the GitHub Packages repository and the following dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/CodeAndCup/CupCodeAPI</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>fr.perrier</groupId>
        <artifactId>cupcodeapi</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```
Replace `VERSION` with the desired version.  
Don't forget to configure your GitHub credentials for repository access if needed.

## Documentation

Full documentation, usage examples, and integration guides are available on the [project Wiki](https://docs.cupcode.fr).

## Support

For any questions or suggestions, open an issue on GitHub or join our Discord.

---
Développé avec ❤️ pour la communauté Minecraft.
