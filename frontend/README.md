# Projet SOA - Gestion des Personnes

Application web complète pour la gestion CRUD de personnes, développée avec **React**, **JAX-RS (Jersey)**, **JPA/Hibernate** et **MySQL**.

##  Table des Matières

1. [Architecture du Projet](#architecture-du-projet)
2. [Technologies Utilisées](#technologies-utilisées)
3. [Backend - JAX-RS + JPA/Hibernate](#backend---jax-rs--jpa-hibernate)
4. [Frontend - React](#frontend---react)
5. [Communication Frontend-Backend](#communication-frontend-backend)
6. [Installation et Configuration](#installation-et-configuration)
7. [Utilisation](#utilisation)  
8. [Structure des Dossiers](#structure-des-dossiers)

---

##  Architecture du Projet

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
│  │  │    - GET/POST/PUT/DELETE via EntityManager       │  │  │
│  │  │    - Transactions locales avec JPA               │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  │                         ↕                                │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │    Couche JPA/Hibernate                          │  │  │
│  │  │    - Person.java (@Entity, @NamedQuery)          │  │  │
│  │  │    - JPAUtil (EntityManagerFactory)              │  │  │
│  │  │    - persistence.xml (personPU)                  │  │  │
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
- **JPA 2.2 + Hibernate 5.6** - Mapping objet-relationnel et transactions
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

## 🔧 Backend - JAX-RS + JPA/Hibernate

### Architecture Backend

Le backend reste basé sur **JAX-RS/Jersey**, mais la persistance est désormais gérée par **JPA 2.2** avec **Hibernate 5.6**. Cette couche apporte :

- Un mapping objet-relationnel grâce aux annotations standard (@Entity, @NamedQuery, etc.).
- Une gestion transactionnelle plus sûre (`EntityTransaction`).
- L'accès à la base via l'`EntityManager` plutôt que du SQL manuel.

#### 1. **RestApplication.java** - Point d'entrée JAX-RS

```java
@ApplicationPath("/api")
public class RestApplication extends Application { }
```

- Définit le préfixe `/api` commun à tous les endpoints.
- Jersey découvre automatiquement les ressources dans le package.

#### 2. **PersonResource.java** - Contrôleur REST propulsé par JPA

Chaque endpoint ouvre un `EntityManager` via `JPAUtil`, exécute l'opération JPA équivalente, puis ferme proprement les ressources. Exemple pour la lecture :

```java
@GET
public List<Person> getAllPersons() {
  EntityManager em = JPAUtil.getEntityManager();
  try {
    return em.createNamedQuery("Person.findAll", Person.class).getResultList();
  } finally {
    em.close();
  }
}
```

Création avec transaction locale :

```java
@POST
public Response addPerson(Person person) {
  EntityManager em = JPAUtil.getEntityManager();
  EntityTransaction tx = em.getTransaction();
  try {
    tx.begin();
    em.persist(person); // Hibernate auto-génère l'ID
    tx.commit();
    return Response.status(Response.Status.CREATED).entity(person).build();
  } catch (Exception e) {
    if (tx.isActive()) tx.rollback();
    throw new WebApplicationException("Persistence error: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
  } finally {
    em.close();
  }
}
```

Le même schéma s'applique aux mises à jour (`em.find`, puis modification de l'entité gérée) et aux suppressions (`em.remove`). Les transactions locales garantissent la cohérence même en cas d'exception.

#### 3. **Person.java** - Entité JPA

```java
@Entity
@Table(name = "persons")
@NamedQueries({
  @NamedQuery(name = "Person.findAll", query = "SELECT p FROM Person p ORDER BY p.id"),
  @NamedQuery(name = "Person.searchByName", query = "SELECT p FROM Person p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
})
public class Person {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false)
  private Integer age;

  // Getters/Setters classiques
}
```

- Les `@NamedQuery` réutilisables simplifient les lectures.
- `GenerationType.IDENTITY` délègue la génération de l'ID à MySQL.

#### 4. **persistence.xml** - Configuration de l'unité de persistance

```xml
<persistence-unit name="personPU" transaction-type="RESOURCE_LOCAL">
  <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
  <class>com.example.model.Person</class>
  <properties>
    <property name="javax.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
    <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/person_db"/>
    <property name="javax.persistence.jdbc.user" value="root"/>
    <property name="javax.persistence.jdbc.password" value="1234"/>
    <property name="hibernate.dialect" value="org.hibernate.dialect.MySQL8Dialect"/>
    <property name="hibernate.hbm2ddl.auto" value="update"/>
    <property name="hibernate.show_sql" value="true"/>
  </properties>
</persistence-unit>
```

- `RESOURCE_LOCAL` : transactions gérées par l'application (sans JTA).
- `hibernate.hbm2ddl.auto=update` synchronise le schéma à chaque lancement (pratique en dev).

#### 5. **JPAUtil.java** - Fabrique d'EntityManager

```java
public final class JPAUtil {
  private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("personPU");

  public static EntityManager getEntityManager() {
    return EMF.createEntityManager();
  }

  public static void close() {
    if (EMF.isOpen()) EMF.close();
  }
}
```

- Centralise l'`EntityManagerFactory` (initialisation coûteuse) et fournit des `EntityManager` prêts à l'emploi.
- `PersonResource` ferme l'usine via `@PreDestroy` pour éviter les fuites lors de l'arrêt de l'application.

#### 6. **CORSFilter.java** - Gestion CORS

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

  useEffect(() => {
    if (editingPerson) {
      setFormData({ id: editingPerson.id, name: editingPerson.name, age: editingPerson.age });
    }
  }, [editingPerson]);

  const validate = () => {
    const newErrors = {};
    if (formData.name.trim().length < 2) newErrors.name = 'Name must have at least 2 characters';
    if (!Number.isInteger(Number(formData.age)) || formData.age < 0) newErrors.age = 'Age must be a non-negative integer';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    const payload = {
      name: formData.name.trim(),
      age: Number(formData.age)
    };

    const success = editingPerson
      ? await onUpdate(editingPerson.id, payload)
      : await onAdd(payload);

    if (success) handleReset();
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: undefined });
  };

  const handleReset = () => {
    setFormData({ id: '', name: '', age: '' });
    setErrors({});
    if (editingPerson) onCancel();
  };

  return (
    <form onSubmit={handleSubmit}>
      {editingPerson && (
        <input type="number" name="id" value={formData.id} disabled />
      )}
      <input type="text" name="name" value={formData.name} onChange={handleChange} />
      <input type="number" name="age" value={formData.age} onChange={handleChange} />
      <button type="submit">{editingPerson ? 'Update' : 'Save'}</button>
    </form>
  );
}
```

**Validation côté client :**
- Name : Minimum 2 caractères
- Age : Entier non négatif
- L'ID est verrouillé et uniquement affiché lors d'une édition

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
  Body: {"name":"John","age":30}
   
   ↓
6. Tomcat reçoit la requête et la route vers Jersey
   ↓
7. Jersey désérialise le JSON en objet Person
   ↓
8. @POST addPerson(Person person) est appelé dans PersonResource.java
   ↓
9. PersonResource ouvre un EntityManager, démarre une transaction et invoque `em.persist(person)`
  ↓
10. Hibernate synchronise l'entité avec MySQL (génération de l'ID)
   ↓
11. PersonResource valide la transaction et crée une Response 201 CREATED
    ↓
12. Jersey sérialise l'objet Person (désormais avec son ID) en JSON
    
    Response: 201 Created
   Body: {"id":7,"name":"John","age":30}
    
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
@GET
@Path("/{id}")
public Response getPersonById(@PathParam("id") int id) {
  EntityManager em = JPAUtil.getEntityManager();
  try {
    Person person = em.find(Person.class, id);
    return person != null
      ? Response.ok(person).build()
      : Response.status(Response.Status.NOT_FOUND).build();
  } finally {
    em.close();
  }
}
```

#### Erreur de persistance

```java
EntityManager em = JPAUtil.getEntityManager();
EntityTransaction tx = em.getTransaction();
try {
  tx.begin();
  em.persist(person);
  tx.commit();
} catch (Exception e) {
  if (tx.isActive()) tx.rollback();
  throw new WebApplicationException(
    "Persistence error: " + e.getMessage(),
    Response.Status.INTERNAL_SERVER_ERROR
  );
} finally {
  em.close();
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

Éditer `src/main/resources/META-INF/persistence.xml` et adapter les propriétés suivantes :

```xml
<property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/person_db"/>
<property name="javax.persistence.jdbc.user" value="root"/>
<property name="javax.persistence.jdbc.password" value="1234"/>
```

**b) Vérifier la base (optionnel)**

- Laisser `hibernate.hbm2ddl.auto=update` pour que Hibernate crée/ajuste la table au démarrage.
- Ou exécuter `database/schema.sql` manuellement si vous préférez un contrôle total du schéma.

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
- Remplir le formulaire (Name, Age)
- Cliquer "Save"
- Validation automatique :
  - Name : Minimum 2 caractères
  - Age : Entier ≥ 0
- L'ID est généré automatiquement par MySQL/Hibernate

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
│       │           ├── filter/
│       │           │   └── CORSFilter.java            # Filtre CORS
│       │           ├── model/
│       │           │   └── Person.java                # Modèle de données
│       │           └── resource/
│       │               └── PersonResource.java        # Endpoints REST
│       │           └── util/
│       │               └── JPAUtil.java               # Fabrique EntityManager
│       └── webapp/
│           ├── index.html                             # Page HTML (React)
│           ├── bundle.js                              # JavaScript compilé
│           ├── META-INF/
│           └── WEB-INF/
│               └── web.xml                            # Configuration Servlet
│       └── resources/
│           └── META-INF/persistence.xml               # Unité de persistance personPU
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

✅ **Paramètres JPA nommés** : liaisons sécurisées via NamedQuery
```java
TypedQuery<Person> query = em.createNamedQuery("Person.searchByName", Person.class);
query.setParameter("name", name.trim());
```

✅ **Transactions explicites** : rollback automatique en cas d'échec
```java
EntityTransaction tx = em.getTransaction();
try {
  tx.begin();
  em.persist(person);
  tx.commit();
} catch (Exception e) {
  if (tx.isActive()) tx.rollback();
  throw e;
}
```

✅ **Fermeture garantie des EntityManager** (`try/finally`)

✅ **Gestion des erreurs** : codes HTTP cohérents (200, 201, 404, 500)

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
$person = @{ name = "Jean"; age = 35 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/person-backend1/api/persons" `
  -ContentType "application/json" -Body $person

# PUT (update)
$person = @{ name = "Jean Martin"; age = 36 } | ConvertTo-Json
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
