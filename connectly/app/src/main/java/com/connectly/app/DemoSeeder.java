package com.connectly.app;

import com.connectly.application.UseCases.*;
import com.connectly.domain.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

/**
 * Demo podaci: ~25 korisnika sa objavama, slikama, komentarima, odgovorima, lajkovima, praćenjima i porukama.
 * Uključuje se sa DEMO_SEED=true i pokreće se samo jednom (preskače ako demo nalozi već postoje).
 * Svi demo nalozi imaju lozinku iz DEMO_PASSWORD (podrazumevano Demo12345!).
 */
@Component
@Order(100)
class DemoSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoSeeder.class);

    private static final String[][] PEOPLE = {
        {"Ana", "Petrović"}, {"Marko", "Jovanović"}, {"Jelena", "Nikolić"}, {"Nikola", "Ilić"},
        {"Milica", "Stojanović"}, {"Stefan", "Marković"}, {"Teodora", "Đorđević"}, {"Luka", "Pavlović"},
        {"Sara", "Kostić"}, {"Vuk", "Popović"}, {"Nina", "Simić"}, {"Filip", "Lazić"},
        {"Katarina", "Todorović"}, {"Aleksa", "Mitić"}, {"Lena", "Obradović"}, {"Đorđe", "Ristić"},
        {"Mina", "Stanković"}, {"Petar", "Vasić"}, {"Dunja", "Savić"}, {"Miloš", "Tomić"},
        {"Isidora", "Kovačević"}, {"Andrej", "Bogdanović"}, {"Višnja", "Marinković"}, {"Uroš", "Živković"},
        {"Tara", "Jakšić"}
    };
    private static final String[] POSTS = {
        "Jutarnja kafa i pogled na reku. Idealan početak dana ☕", "Konačno vikend! Ko ide na planinu?",
        "Napravila sam prvi put domaći hleb i uspelo je 🍞", "Novi projekat na poslu, uzbuđen sam!",
        "Šetnja kroz Kalemegdan u zalasku sunca.", "Preporuka: pročitajte ovu knjigu, ne možete da je ostavite.",
        "Trening završen, noge više ne osećam 😅", "Najbolji ćevapi u gradu — ne raspravljamo.",
        "Prvi sneg u godini! ❄️", "Završio sam kurs programiranja. Hvala svima na podršci!",
        "Koncert sinoć je bio nezaboravan 🎸", "Putovanje na more se bliži, odbrojavam dane.",
        "Ko ima savet za početnika u fotografiji?", "Mala radost: miris kiše i tople čaj.",
        "Dan za učenje, kafu i dobru muziku.", "Moj pas je danas naučio novi trik 🐶",
        "Kuvam ručak za celu porodicu, mirisi su neverovatni.", "Rođendan prijatelja — slavlje do jutra!",
        "Biciklom do posla, 12 km čistog uživanja.", "Gledali smo film noćas, preporuka za vikend."
    };
    private static final String[] COMMENTS = {
        "Prelepo! 😍", "Potpuno se slažem.", "Odlična fotka!", "Bravo, svaka čast!", "Haha, tačno tako!",
        "Moram i ja da probam.", "Želim ti sve najbolje!", "Super vest!", "Gde je ovo snimljeno?",
        "Predivno, uživaj!", "Kad idemo opet?", "Mnogo inspirativno."
    };
    private static final String[] REPLIES = {"Hvala ti puno! 🙌", "Naravno, javi se!", "Ha, hvala!", "Slažem se 🙂", "Uskoro!"};
    private static final String[] DMS = {
        "Hej, kako si?", "Dobro sam, a ti?", "Vidimo se uskoro?", "Može, javi kad si slobodan.", "Super, dogovoreno!"
    };

    private final Register register;
    private final FollowUser follow;
    private final CreatePost createPost;
    private final LikePost likePost;
    private final AddComment addComment;
    private final LikeComment likeComment;
    private final MessageOps messages;
    private final BookmarkOps bookmarks;
    private final UserRepository users;
    private final boolean enabled;
    private final String password;

    DemoSeeder(Register register, FollowUser follow, CreatePost createPost, LikePost likePost,
               AddComment addComment, LikeComment likeComment, MessageOps messages, BookmarkOps bookmarks,
               UserRepository users,
               @Value("${connectly.demo.seed:false}") boolean enabled,
               @Value("${connectly.demo.password:Demo12345!}") String password) {
        this.register = register; this.follow = follow; this.createPost = createPost; this.likePost = likePost;
        this.addComment = addComment; this.likeComment = likeComment; this.messages = messages;
        this.bookmarks = bookmarks; this.users = users; this.enabled = enabled; this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;
        if (users.existsByUsername(username(PEOPLE[0]))) {
            log.info("Demo podaci već postoje, preskačem.");
            return;
        }
        Random rnd = new Random(42);
        List<Long> ids = new ArrayList<>();
        for (String[] p : PEOPLE) {
            String u = username(p);
            ids.add(register.register(new RegisterCommand(p[0], p[1], u, u + "@demo.connectly", password)).userId());
        }
        int n = ids.size();

        // praćenja: svako prati 6–12 drugih
        for (Long a : ids) {
            Set<Long> picked = new HashSet<>();
            int k = 6 + rnd.nextInt(7);
            while (picked.size() < k) {
                Long b = ids.get(rnd.nextInt(n));
                if (!b.equals(a) && picked.add(b)) follow.follow(a, b);
            }
        }

        // objave (2–4 po korisniku, ~60% sa slikom)
        List<PostView> posts = new ArrayList<>();
        for (Long a : ids) {
            int count = 2 + rnd.nextInt(3);
            for (int i = 0; i < count; i++) {
                String text = POSTS[rnd.nextInt(POSTS.length)];
                List<ImageUpload> imgs = rnd.nextInt(10) < 6 ? List.of(image(rnd)) : List.of();
                posts.add(createPost.create(a, text, imgs));
            }
        }

        // lajkovi, komentari, odgovori, lajkovi komentara, sačuvano
        for (PostView p : posts) {
            for (Long u : ids) {
                if (!u.equals(p.authorId()) && rnd.nextInt(100) < 35) likePost.like(p.id(), u);
            }
            int cc = rnd.nextInt(5);
            for (int i = 0; i < cc; i++) {
                Long commenter = ids.get(rnd.nextInt(n));
                CommentView c = addComment.add(p.id(), commenter, null, COMMENTS[rnd.nextInt(COMMENTS.length)]);
                for (Long u : ids) if (rnd.nextInt(100) < 15) likeComment.like(c.id(), u);
                if (rnd.nextInt(100) < 45) {
                    addComment.add(p.id(), p.authorId(), c.id(), REPLIES[rnd.nextInt(REPLIES.length)]);
                }
            }
            if (rnd.nextInt(100) < 12) bookmarks.save(p.id(), ids.get(rnd.nextInt(n)));
        }

        // poruke: 15 razgovora sa 2–5 poruka
        for (int i = 0; i < 15; i++) {
            Long a = ids.get(rnd.nextInt(n));
            Long b = ids.get(rnd.nextInt(n));
            if (a.equals(b)) continue;
            int m = 2 + rnd.nextInt(4);
            for (int j = 0; j < m; j++) {
                messages.send(j % 2 == 0 ? a : b, j % 2 == 0 ? b : a, DMS[j % DMS.length]);
            }
        }
        log.warn("Ubačeno {} demo korisnika i {} objava. Prijava: <korisnicko.ime>@demo.connectly / {}",
                n, posts.size(), password);
    }

    private static String username(String[] p) {
        return (p[0] + "." + p[1]).toLowerCase()
                .replace("đ", "dj").replace("š", "s").replace("č", "c").replace("ć", "c").replace("ž", "z");
    }

    /** Jednostavna gradijent slika (JPEG) da feed ima vizuelni sadržaj bez eksternih fajlova. */
    private static ImageUpload image(Random rnd) {
        System.setProperty("java.awt.headless", "true");
        int w = 900, h = 700;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Color c1 = Color.getHSBColor(rnd.nextFloat(), 0.55f, 0.95f);
        Color c2 = Color.getHSBColor(rnd.nextFloat(), 0.65f, 0.80f);
        g.setPaint(new GradientPaint(0, 0, c1, w, h, c2));
        g.fillRect(0, 0, w, h);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(255, 255, 255, 25 + rnd.nextInt(40)));
            int d = 120 + rnd.nextInt(380);
            g.fillOval(rnd.nextInt(w) - d / 2, rnd.nextInt(h) - d / 2, d, d);
        }
        g.dispose();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", out);
            return new ImageUpload(out.toByteArray(), "image/jpeg");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
