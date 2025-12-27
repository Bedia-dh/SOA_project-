# Projet SOA - Gestion des Personnes

Application web complète pour la gestion CRUD de personnes, développée avec **React**, **JAX-RS (Jersey)**, **JDBC** et **MySQL**.

## 📋 Table des Matières

1. [Architecture du Projet](#architecture-du-projet)
2. [Technologies Utilisées](#technologies-utilisées)
3. [Backend - JAX-RS avec JDBC](#backend---jax-rs-avec-jdbc)
4. [Frontend - React](#frontend---react)
5. [Communication Frontend-Backend](#communication-frontend-backend)
6. [Installation et Configuration](#installation-et-configuration)
7. [Utilisation](#utilisation)
8. [Structure des Dossiers](#structure-des-dossiers)

---

## 🏗️ Architecture du Projet

Le projet suit une architecture **client-serveur** séparant clairement le frontend et le backend :

```
┌─────────────────────────────────────────────────────────────┐
│                    Navigateur Web (Client)                   │
│                                                               │
│  ┌────────────────────────────────────────────────────────┐  │
│  │              React Application (SPA)                    │  │
│  │  - Components (PersonTable, PersonForm, SearchBar)     │  │
│  │  - Service API (personService.js)                      │  │
│  │  - State Management (useState, useEffect)              │  │
│  └────────────────────────────────────────────────────────┘  │
│                            ↕                                  │
│                    HTTP REST Requests                         │
│                    (JSON format)                              │
└─────────────────────────────────────────────────────────────┘
                             ↕
┌─────────────────────────────────────────────────────────────┐
│              Serveur Tomcat 9.0.86 (WAR)                     │
│                                                               │
│  ┌────────────────────────────────────────────────────────┐  │
│  │         Jersey (JAX-RS Implementation)                  │  │
│  │                                                          │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │    PersonResource.java (REST Endpoints)          │  │  │
│  │  │    @Path("/api/persons")                         │  │  │
│  │  │    - GET    /api/persons                         │  │  │
│  │  │    - GET    /api/persons/{id}                    │  │  │
│  │  │    - GET    /api/persons/search?name=...         │  │  │
│  │  │    - POST   /api/persons                         │  │  │
│  │  │    - PUT    /api/persons/{id}                    │  │  │
│  │  │    - DELETE /api/persons/{id}                    │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  │                         ↕                                │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │    DatabaseConnection.java (JDBC)                │  │  │
│  │  │    - Gestion des connexions MySQL                │  │  │
│  │  │    - Exécution des requêtes SQL                  │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                             ↕
┌─────────────────────────────────────────────────────────────┐
│                    Base de Données MySQL                     │
│                                                               │
│  Database: person_db                                          │
│  Table: persons (id, name, age)                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Technologies Utilisées

### Backend
- **Java 8** - Langage de programmation
- **JAX-RS (Jersey 2.35)** - API REST standard Java
- **JDBC (MySQL Connector 8.0.33)** - Connexion à la base de données
- **Servlet API 3.1** - Conteneur web
- **Apache Tomcat 9.0.86** - Serveur d'applications
- **Maven 3.9.6** - Gestion de dépendances et build

### Frontend
- **React 18** - Framework JavaScript UI
- **Webpack 5** - Bundler de modules
- **Babel** - Transpileur JavaScript (ES6+ → ES5)
- **CSS3** - Stylisation (design moderne avec variables CSS)

### Base de Données
- **MySQL 8.0** - Système de gestion de base de données relationnelle

---

## 🔧 Backend - JAX-RS avec JDBC

### Architecture Backend

Le backend utilise **JAX-RS** (Java API for RESTful Web Services) avec l'implémentation **Jersey** pour exposer des services REST.

#### 1. **RestApplication.java** - Point d'entrée JAX-RS

```java
@ApplicationPath("/api")
public class RestApplication extends Application {
}
```

- `@ApplicationPath("/api")` : Définit le chemin de base pour tous les endpoints REST
- Toutes les ressources seront accessibles sous `/api/*`

#### 2. **PersonResource.java** - Contrôleur REST

Le contrôleur expose 6 endpoints REST :

##### **a) GET /api/persons - Récupérer toutes les personnes**

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public List<Person> getAllPersons() {
    String sql = "SELECT id, name, age FROM persons";
    // Exécution de la requête SQL via JDBC
    // Conversion des ResultSet en objets Person
    // Retour automatique en JSON par Jersey
}
```

##### **b) GET /api/persons/{id} - Récupérer une personne par ID**

```java
@GET
@Path("/{id}")
public Response getPersonById(@PathParam("id") int id) {
    String sql = "SELECT id, name, age FROM persons WHERE id = ?";
    // PreparedStatement pour éviter les injections SQL
    // Retourne 200 OK avec la personne ou 404 NOT_FOUND
}
```

##### **c) GET /api/persons/search?name=X - Rechercher par nom**

```java
@GET
@Path("/search")
public List<Person> getPersonByName(@QueryParam("name") String name) {
    String sql = "SELECT id, name, age FROM persons WHERE name LIKE ?";
    // Recherche avec LIKE pour correspondance partielle
    // Retourne une liste (peut être vide)
}
```

##### **d) POST /api/persons - Créer une personne**

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response addPerson(Person person) {
    String sql = "INSERT INTO persons (name, age) VALUES (?, ?)";
    // Insertion avec auto-génération de l'ID
    // Retourne 201 CREATED avec la personne créée
}
```

##### **e) PUT /api/persons/{id} - Mettre à jour une personne**

```java
@PUT
@Path("/{id}")
public Response updatePerson(@PathParam("id") int id, Person person) {
    String sql = "UPDATE persons SET name = ?, age = ? WHERE id = ?";
    // Mise à jour conditionnelle
    // Retourne 200 OK ou 404 si ID inexistant
}
```

##### **f) DELETE /api/persons/{id} - Supprimer une personne**

```java
@DELETE
@Path("/{id}")
public Response deletePerson(@PathParam("id") int id) {
    String sql = "DELETE FROM persons WHERE id = ?";
    // Suppression sécurisée avec PreparedStatement
    // Retourne 204 NO_CONTENT ou 404
}
```

#### 3. **DatabaseConnection.java** - Gestion JDBC

```java
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/person_db";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

**Rôle :**
- Centralise la configuration de connexion MySQL
- Fournit des connexions à la demande (pattern Factory)
- Les connexions sont fermées avec `try-with-resources` pour éviter les fuites

#### 4. **Person.java** - Modèle de données

```java
public class Person {
    private int id;
    private String name;
    private int age;
    
    // Constructeurs, getters, setters
}
```

- POJO (Plain Old Java Object) sans annotations JPA
- Jersey le sérialise/désérialise automatiquement en JSON via JSON-B

#### 5. **CORSFilter.java** - Gestion CORS

```java
@Provider
public class CORSFilter implements ContainerResponseFilter {
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        res.getHeaders().add("Access-Control-Allow-Origin", "*");
        res.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        // ...
    }
}
```

**Pourquoi CORS ?**
- Permet au frontend (domaine différent en développement) d'appeler l'API
- En production (même WAR), CORS n'est pas nécessaire mais ne pose pas de problème

---

## ⚛️ Frontend - React

### Architecture Frontend

Le frontend est une **Single Page Application (SPA)** React organisée en composants réutilisables.

#### Structure des Composants

```
App.js (Composant racine)
├── Header.js (Titre + statut)
├── SearchBar.js (Recherche par nom/ID)
├── PersonTable.js (Affichage tableau)
└── PersonForm.js (Formulaire ajout/modification)
```

#### 1. **App.js** - Composant principal

```javascript
function App() {
  const [persons, setPersons] = useState([]);
  const [status, setStatus] = useState({ message: 'Ready', isError: false });
  const [editingPerson, setEditingPerson] = useState(null);

  useEffect(() => {
    loadPersons(); // Chargement initial
  }, []);

  const loadPersons = async () => {
    const data = await personService.getAll();
    setPersons(data);
  };

  // Handlers pour CRUD...
}
```

**Gestion d'état :**
- `persons` : Liste des personnes affichées
- `status` : Message de statut (succès/erreur)
- `editingPerson` : Personne en cours de modification (null sinon)

#### 2. **personService.js** - Couche d'accès API

```javascript
const API_BASE = '/api/persons';

export const personService = {
  async getAll() {
    const res = await fetch(API_BASE);
    return res.json();
  },

  async getById(id) {
    const res = await fetch(`${API_BASE}/${id}`);
    if (res.status === 404) return null;
    return res.json();
  },

  async create(person) {
    const res = await fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(person)
    });
    return res.json();
  },

  // update, delete, searchByName...
};
```

**Rôle :**
- Encapsule toutes les requêtes HTTP vers le backend
- Utilise l'API Fetch native du navigateur
- Gère la sérialisation/désérialisation JSON
- Centralise la gestion des erreurs HTTP

#### 3. **PersonTable.js** - Affichage des données

```javascript
function PersonTable({ persons, onEdit, onDelete, onRefresh }) {
  return (
    <table>
      <tbody>
        {persons.map((person) => (
          <tr key={person.id}>
            <td>{person.id}</td>
            <td>{person.name}</td>
            <td>{person.age}</td>
            <td>
              <button onClick={() => onEdit(person)}>Edit</button>
              <button onClick={() => onDelete(person.id)}>Delete</button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

**Props :**
- `persons` : Données à afficher
- `onEdit`, `onDelete`, `onRefresh` : Callbacks vers le parent (App.js)

#### 4. **PersonForm.js** - Formulaire CRUD

```javascript
function PersonForm({ editingPerson, onAdd, onUpdate, onCancel }) {
  const [formData, setFormData] = useState({ id: '', name: '', age: '' });
  const [errors, setErrors] = useState({});

  const validate = () => {
    const newErrors = {};
    if (!Number.isInteger(Number(formData.id)) || formData.id < 1) {
      newErrors.id = 'ID must be a positive integer';
    }
    if (formData.name.trim().length < 2) {
      newErrors.name = 'Name must have at least 2 characters';
    }
    // ...
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    
    const person = {
      id: Number(formData.id),
      name: formData.name.trim(),
      age: Number(formData.age)
    };

    if (editingPerson) {
      await onUpdate(editingPerson.id, person);
    } else {
      await onAdd(person);
    }
  };
}
```

**Validation côté client :**
- ID : Entier positif
- Name : Minimum 2 caractères
- Age : Entier non négatif

#### 5. **SearchBar.js** - Recherche intelligente

```javascript
function SearchBar({ onSearch, onClear }) {
  const [query, setQuery] = useState('');

  const handleSearch = () => {
    onSearch(query); // Délègue la logique au parent
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') handleSearch();
  };
}
```

**Logique de recherche (dans App.js) :**
- Si query est un nombre → Recherche par ID
- Sinon → Recherche par nom (LIKE)

---

## 🔗 Communication Frontend-Backend

### Flux de données complet

#### Exemple : Ajout d'une personne

```
1. Utilisateur remplit le formulaire et clique "Save"
   ↓
2. PersonForm.js valide les données côté client
   ↓
3. handleSubmit() appelle onAdd(person) (callback vers App.js)
   ↓
4. App.js appelle personService.create(person)
   ↓
5. personService.js envoie une requête HTTP POST
   
   POST /api/persons
   Content-Type: application/json
   Body: {"id":3,"name":"John","age":30}
   
   ↓
6. Tomcat reçoit la requête et la route vers Jersey
   ↓
7. Jersey désérialise le JSON en objet Person
   ↓
8. @POST addPerson(Person person) est appelé dans PersonResource.java
   ↓
9. PersonResource exécute la requête SQL via JDBC
   
   INSERT INTO persons (name, age) VALUES ('John', 30)
   
   ↓
10. MySQL insère la ligne et retourne l'ID auto-généré
    ↓
11. PersonResource crée une Response avec statut 201 CREATED
    ↓
12. Jersey sérialise l'objet Person en JSON
    
    Response: 201 Created
    Body: {"id":3,"name":"John","age":30}
    
    ↓
13. personService.js reçoit la réponse et parse le JSON
    ↓
14. App.js met à jour le state et recharge la liste
    ↓
15. React re-render PersonTable avec les nouvelles données
    ↓
16. L'utilisateur voit la nouvelle personne dans le tableau
```

### Formats d'échange

#### Requête POST/PUT (Frontend → Backend)

```http
POST /api/persons HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "id": 3,
  "name": "Marie Dupont",
  "age": 28
}
```

#### Réponse GET (Backend → Frontend)

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
  {"id": 1, "name": "Ahmed", "age": 21},
  {"id": 2, "name": "Sara", "age": 23},
  {"id": 3, "name": "Marie Dupont", "age": 28}
]
```

### Gestion des erreurs

#### Erreur 404 - Personne non trouvée

```javascript
// Frontend
try {
  const person = await personService.getById(999);
  if (person === null) {
    setStatus('No person found with ID: 999');
  }
} catch (err) {
  setStatus(err.message, true); // Erreur serveur
}
```

```java
// Backend
@GET
@Path("/{id}")
public Response getPersonById(@PathParam("id") int id) {
    // ...
    if (rs.next()) {
        return Response.ok(person).build(); // 200 OK
    } else {
        return Response.status(Response.Status.NOT_FOUND).build(); // 404
    }
}
```

#### Erreur SQL

```java
// Backend
try (Connection conn = DatabaseConnection.getConnection()) {
    // ...
} catch (SQLException e) {
    e.printStackTrace();
    throw new WebApplicationException(
        "Database error: " + e.getMessage(), 
        Response.Status.INTERNAL_SERVER_ERROR // 500
    );
}
```

---

## 🚀 Installation et Configuration

### Prérequis

- **Java JDK 8+** installé
- **MySQL 8.0+** installé et démarré
- **Node.js 16+** et npm (pour le frontend)
- **Git** (optionnel)

### 1. Configuration de la base de données

```sql
-- Ouvrir MySQL Workbench et exécuter :

CREATE DATABASE IF NOT EXISTS person_db;
USE person_db;

CREATE TABLE IF NOT EXISTS persons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL
);

INSERT INTO persons (name, age) VALUES 
    ('Ahmed', 21),
    ('Sara', 23);
```

### 2. Configuration backend

**a) Mettre à jour les identifiants MySQL**

Éditer `src/main/java/com/example/db/DatabaseConnection.java` :

```java
private static final String USER = "root";        // Votre utilisateur MySQL
private static final String PASSWORD = "1234";     // Votre mot de passe MySQL
```

**b) Tester la connexion (optionnel)**

```powershell
cd C:\Users\wmdai\eclipse-workspace\person-backend1
$cp = "target\person-backend1\WEB-INF\lib\*;target\classes"
javac -encoding UTF-8 -cp $cp -d target\classes src\main\java\com\example\db\DatabaseConnection.java
java -cp "$cp;target\classes" com.example.db.DatabaseConnection
```

Devrait afficher :
```
✓ Connected to MySQL server
✓ Database 'person_db' created/exists
✓ Table 'persons' created/exists
✓ Sample data inserted
```

### 3. Build du projet

**a) Build du frontend React**

```powershell
cd frontend
npm install
npm run build
```

**b) Copier le build dans webapp**

```powershell
cd ..
Copy-Item frontend\dist\* src\main\webapp\ -Recurse -Force
```

**c) Build du WAR avec Maven**

```powershell
.\mvnw.cmd clean package
```

Le fichier `target/person-backend1.war` est créé.

### 4. Déploiement sur Tomcat

**Option A : Tomcat intégré (déjà configuré)**

```powershell
# Copier le WAR
Copy-Item target\person-backend1.war server\apache-tomcat-9.0.86\webapps\person-backend1.war -Force

# Démarrer Tomcat
cd server\apache-tomcat-9.0.86\bin
.\catalina.bat start
```

**Option B : Tomcat externe**

1. Copier `target/person-backend1.war` dans votre dossier Tomcat `webapps/`
2. Démarrer Tomcat
3. Le WAR sera automatiquement déployé

### 5. Accès à l'application

Ouvrir le navigateur :

```
http://localhost:8080/person-backend1/
```

---

## 📖 Utilisation

### Opérations CRUD

#### 1. **Consulter** toutes les personnes
- La liste s'affiche automatiquement au chargement
- Cliquer sur "Refresh" pour recharger

#### 2. **Rechercher** une personne
- Entrer un **ID** (ex: `1`) → Recherche exacte par ID
- Entrer un **nom** (ex: `Ahmed`) → Recherche par nom (LIKE)
- Cliquer "Search" ou appuyer sur Entrée
- Cliquer "Clear" pour revenir à la liste complète

#### 3. **Ajouter** une personne
- Remplir le formulaire (ID, Name, Age)
- Cliquer "Save"
- Validation automatique :
  - ID : Entier positif
  - Name : Minimum 2 caractères
  - Age : Entier ≥ 0

#### 4. **Modifier** une personne
- Cliquer sur "Edit" dans la ligne de la personne
- Le formulaire se pré-remplit avec les données existantes
- Modifier les champs
- Cliquer "Update"

#### 5. **Supprimer** une personne
- Cliquer sur "Delete" dans la ligne de la personne
- Confirmer la suppression dans la boîte de dialogue
- La personne est supprimée de la base de données

---

## 📁 Structure des Dossiers

```
person-backend1/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           ├── config/
│       │           │   └── RestApplication.java       # Configuration JAX-RS
│       │           ├── db/
│       │           │   └── DatabaseConnection.java    # Gestion JDBC
│       │           ├── filter/
│       │           │   └── CORSFilter.java            # Filtre CORS
│       │           ├── model/
│       │           │   └── Person.java                # Modèle de données
│       │           └── resource/
│       │               └── PersonResource.java        # Endpoints REST
│       └── webapp/
│           ├── index.html                             # Page HTML (React)
│           ├── bundle.js                              # JavaScript compilé
│           ├── META-INF/
│           └── WEB-INF/
│               └── web.xml                            # Configuration Servlet
│
├── frontend/                                          # Code source React
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/
│   │   │   ├── Header.js
│   │   │   ├── PersonForm.js
│   │   │   ├── PersonTable.js
│   │   │   └── SearchBar.js
│   │   ├── services/
│   │   │   └── personService.js                      # API calls
│   │   ├── App.js                                    # Composant principal
│   │   ├── App.css                                   # Styles
│   │   └── index.js                                  # Point d'entrée
│   ├── dist/                                         # Build production
│   ├── package.json
│   └── webpack.config.js
│
├── database/
│   └── schema.sql                                    # Script SQL
│
├── server/
│   └── apache-tomcat-9.0.86/                         # Tomcat local
│
├── target/
│   └── person-backend1.war                           # Fichier WAR déployable
│
├── pom.xml                                           # Configuration Maven
├── mvnw, mvnw.cmd                                    # Maven Wrapper
└── README.md                                         # Ce fichier
```

---

## 🔍 Endpoints API (JAX-RS)

| Méthode | Endpoint | Description | Body Request | Response |
|---------|----------|-------------|--------------|----------|
| GET | `/api/persons` | Récupérer toutes les personnes | - | `200 OK` + JSON array |
| GET | `/api/persons/{id}` | Récupérer une personne par ID | - | `200 OK` + JSON object ou `404 NOT_FOUND` |
| GET | `/api/persons/search?name=X` | Rechercher par nom | - | `200 OK` + JSON array |
| POST | `/api/persons` | Créer une nouvelle personne | JSON object | `201 CREATED` + JSON object |
| PUT | `/api/persons/{id}` | Mettre à jour une personne | JSON object | `200 OK` + JSON object ou `404 NOT_FOUND` |
| DELETE | `/api/persons/{id}` | Supprimer une personne | - | `204 NO_CONTENT` ou `404 NOT_FOUND` |

### Exemples de requêtes

**GET /api/persons**
```http
GET /api/persons HTTP/1.1
Host: localhost:8080
```

**Réponse :**
```json
[
  {"id": 1, "name": "Ahmed", "age": 21},
  {"id": 2, "name": "Sara", "age": 23}
]
```

**POST /api/persons**
```http
POST /api/persons HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "id": 3,
  "name": "Leila",
  "age": 25
}
```

**Réponse :**
```json
HTTP/1.1 201 Created
{
  "id": 3,
  "name": "Leila",
  "age": 25
}
```

---

## 🛡️ Sécurité et Bonnes Pratiques

### Backend

✅ **PreparedStatement** : Protection contre les injections SQL
```java
String sql = "SELECT * FROM persons WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setInt(1, id); // Paramètre sécurisé
```

✅ **Try-with-resources** : Fermeture automatique des connexions
```java
try (Connection conn = DatabaseConnection.getConnection()) {
    // Les ressources sont automatiquement fermées
}
```

✅ **Gestion des erreurs** : Codes HTTP appropriés (200, 201, 404, 500)

✅ **CORS configuré** : Permet les requêtes cross-origin en développement

### Frontend

✅ **Validation côté client** : Vérification avant envoi au serveur

✅ **Confirmation de suppression** : `window.confirm()` avant DELETE

✅ **Gestion des erreurs** : Affichage des messages d'erreur à l'utilisateur

✅ **State management** : useState/useEffect pour réactivité

---

## 🧪 Tests

### Test manuel de l'API avec PowerShell

```powershell
# GET all
Invoke-RestMethod -Uri "http://localhost:8080/person-backend1/api/persons"

# GET by ID
Invoke-RestMethod -Uri "http://localhost:8080/person-backend1/api/persons/1"

# Search by name
Invoke-RestMethod -Uri "http://localhost:8080/person-backend1/api/persons/search?name=Ahmed"

# POST (add)
$person = @{ id = 4; name = "Jean"; age = 35 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/person-backend1/api/persons" `
  -ContentType "application/json" -Body $person

# PUT (update)
$person = @{ id = 4; name = "Jean Martin"; age = 36 } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/person-backend1/api/persons/4" `
  -ContentType "application/json" -Body $person

# DELETE
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/person-backend1/api/persons/4"
```

---

## 🚧 Développement

### Mode développement frontend

Pour développer le frontend avec hot-reload :

```powershell
cd frontend
npm start
```

L'application s'ouvre sur `http://localhost:3000` avec proxy vers le backend sur le port 8080.

### Rebuild rapide

Après modification du frontend :

```powershell
cd frontend
npm run build
cd ..
Copy-Item frontend\dist\* src\main\webapp\ -Recurse -Force
.\mvnw.cmd package -DskipTests
Copy-Item target\person-backend1.war server\apache-tomcat-9.0.86\webapps\ -Force
```

---

## 📝 Auteur

Projet réalisé dans le cadre du cours **SOA (Service-Oriented Architecture)**.

**Technologies maîtrisées :**
- Architecture REST avec JAX-RS
- Gestion de bases de données avec JDBC
- Développement frontend avec React
- Déploiement d'applications Java EE sur Tomcat
- Build automation avec Maven et Webpack

---

## 📄 Licence

Projet éducatif - Usage académique uniquement.
