<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

$host = 'aperichiyk526.mysql.db'; // Adresse du serveur MySQL
$db = 'aperichiyk526'; // Nom de la base
$user = 'aperichiyk526'; // Utilisateur MySQL
$password = 'Cycy100788'; // Mot de passe MySQL

// Récupérer l'email depuis GET ou POST
$email = isset($_GET['email']) ? $_GET['email'] : (isset($_POST['email']) ? $_POST['email'] : null);

// Vérifier si un email a été fourni
if (!$email) {
    echo json_encode(['status' => 'error', 'message' => 'Email manquant']);
    exit;
}

// Vérifier si l'email est valide
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['status' => 'error', 'message' => 'Email invalide']);
    exit;
}

$verificationCode = $verificationCode = bin2hex(openssl_random_pseudo_bytes(16)); // Génère un code aléatoire unique

try {
    // Connexion à la base de données
    $pdo = new PDO("mysql:host=$host;dbname=$db", $user, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Vérifier si l'email existe déjà
    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    if ($stmt->rowCount() > 0) {
        echo json_encode(['status' => 'error', 'message' => 'Cet email est déjà utilisé.']);
        exit;
    }

    // Insérer l'utilisateur et son code de vérification
    $stmt = $pdo->prepare("INSERT INTO users (email, verification_code, is_verified) VALUES (?, ?, ?)");
    if ($stmt->execute([$email, $verificationCode, 0])) {
        $subject = "Confirmez votre email - Tablette Gourmande";
        $link = "https://aperichill.fr/api/verify.php?code=$verificationCode";
        $message = "
        <html>
        <body>
            <p>Bienvenue !</p>
            <p>Pour confirmer votre email, cliquez sur le lien ci-dessous :</p>
            <a href='$link'>$link</a>
        </body>
        </html>";

        $headers = "MIME-Version: 1.0\r\n";
        $headers .= "Content-type:text/html;charset=UTF-8\r\n";
        $headers .= "From: support@tablettegourmande.com\r\n";

        if (mail($email, $subject, $message, $headers)) {
            echo json_encode(['status' => 'success', 'message' => 'Email envoyé avec succès']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Erreur lors de l\'envoi de l\'email']);
        }
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erreur lors de l\'insertion en base']);
    }
} catch (PDOException $e) {
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
}
?>
