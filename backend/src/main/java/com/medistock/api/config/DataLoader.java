package com.medistock.api.config;

import com.medistock.api.models.*;
import com.medistock.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      SupplierRepository supplierRepository,
                      MedicineRepository medicineRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.medicineRepository = medicineRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            logger.info("⚡ [DataLoader] Verifying real-world medical store database items...");

            seedUsers();
            Map<String, Category> categories = seedCategories();
            Map<String, Supplier> suppliers = seedSuppliers();
            seedRealWorldMedicines(categories, suppliers);

            logger.info("✓ [DataLoader] Database successfully primed with real-world medical store catalogue.");
        } catch (Exception e) {
            logger.error("⚠ [DataLoader] Non-fatal error during data seeding: {}", e.getMessage(), e);
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            userRepository.save(new User("admin_01", passwordEncoder.encode("123456"), "admin@medistock.com", "+91 98765 43210", UserRole.ADMIN));
            userRepository.save(new User("pharmacist_01", passwordEncoder.encode("123456"), "pharmacist@medistock.com", "+91 98401 23456", UserRole.PHARMACIST));
            userRepository.save(new User("staff_01", passwordEncoder.encode("123456"), "staff@medistock.com", "+91 98123 45678", UserRole.STAFF));
            logger.info("✓ [DataLoader] Default users created with phone numbers (admin_01, pharmacist_01, staff_01).");
        } else {
            // Ensure all existing users in the database have a realistic phone number
            String[] defaultPhones = {
                "+91 98765 43210", "+91 98401 23456", "+91 98123 45678",
                "+91 97890 12345", "+91 98940 56789", "+91 99400 11223",
                "+91 98844 33221", "+91 97900 44556", "+91 98412 66778"
            };
            List<User> users = userRepository.findAll();
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                if (u.getPhoneNumber() == null || u.getPhoneNumber().isBlank()) {
                    u.setPhoneNumber(defaultPhones[i % defaultPhones.length]);
                    userRepository.save(u);
                }
            }
            logger.info("✓ [DataLoader] Backfilled phone numbers for all existing users.");
        }
    }

    private Map<String, Category> seedCategories() {
        Map<String, Category> categoryMap = new HashMap<>();

        // Fetch existing
        categoryRepository.findAll().forEach(c -> categoryMap.put(c.getName().trim().toLowerCase(), c));

        List<Category> defaults = List.of(
            new Category("Analgesics & Antipyretics", "Pain and fever relievers (Paracetamol, Ibuprofen, Diclofenac)"),
            new Category("Antibiotics & Antimicrobials", "Broad-spectrum antibacterial agents (Amoxicillin, Azithromycin, Cefixime)"),
            new Category("Cardiovascular & Antihypertensives", "Blood pressure, cardiac care, and lipid regulators (Telmisartan, Amlodipine, Atorvastatin)"),
            new Category("Antidiabetics & Endocrine", "Blood glucose regulators and metabolic support (Metformin, Glimepiride, Sitagliptin)"),
            new Category("Gastrointestinal & Antacids", "Acidity, reflux, ulcer healing, and digestive enzymes (Pantoprazole, Omeprazole, Digene, Gelusil)"),
            new Category("Respiratory & Antihistamines", "Cold, allergy, cough, and bronchodilators (Montelukast, Levocetirizine, Cough syrups)"),
            new Category("Dermatologicals & Antiseptics", "Topical creams, ointments, and wound antiseptics (Betadine, Soframycin, Dettol, Volini Gel)"),
            new Category("Vitamins, Minerals & Supplements", "Essential micronutrients and nutritional support (B-Complex, Vitamin C, Calcium D3, Zincovit)"),
            new Category("Ophthalmic & ENT Care", "Eye drops, ear drops, and nasal decongestants (Otrivin, Ciplox Drops, Refresh Tears)"),
            new Category("Surgical Supplies & First Aid", "Bandages, cotton rolls, syringes, test strips, thermometers, and diagnostic devices")
        );

        for (Category cat : defaults) {
            String key = cat.getName().trim().toLowerCase();
            if (!categoryMap.containsKey(key)) {
                Category saved = categoryRepository.save(cat);
                categoryMap.put(key, saved);
            }
        }

        return categoryMap;
    }

    private Map<String, Supplier> seedSuppliers() {
        Map<String, Supplier> supplierMap = new HashMap<>();

        // Fetch existing
        supplierRepository.findAll().forEach(s -> supplierMap.put(s.getName().trim().toLowerCase(), s));

        List<Supplier> defaults = List.of(
            new Supplier("Sun Pharma Distributors Ltd.", "+91 9820011223", "supply@sunpharma.com", "Goregaon East, Mumbai, Maharashtra 400063"),
            new Supplier("Cipla Healthcare Logistics", "+91 9840055443", "orders@cipla.com", "Bellasis Road, Mumbai Central, Maharashtra 400008"),
            new Supplier("Dr. Reddy's Laboratories Network", "+91 9988776655", "distribution@drreddys.com", "Banjara Hills, Hyderabad, Telangana 500034"),
            new Supplier("Torrent Pharmaceuticals Supply", "+91 9712345678", "sales@torrentpharma.com", "Off Ashram Road, Ahmedabad, Gujarat 380009"),
            new Supplier("Abbott Healthcare Logistics", "+91 9811223344", "care@abbottindia.com", "Connaught Place, New Delhi 110001"),
            new Supplier("Mankind Pharma Wholesale", "+91 9910022334", "contact@mankindpharma.com", "Okhla Industrial Area, New Delhi 110020"),
            new Supplier("Lupin Lifesciences Distribution", "+91 9833011229", "logistics@lupin.com", "Santacruz East, Mumbai, Maharashtra 400055"),
            new Supplier("Alkem Laboratories Direct", "+91 9867012345", "supplychain@alkem.com", "Senapati Bapat Marg, Lower Parel, Mumbai 400013")
        );

        for (Supplier sup : defaults) {
            String key = sup.getName().trim().toLowerCase();
            if (!supplierMap.containsKey(key)) {
                Supplier saved = supplierRepository.save(sup);
                supplierMap.put(key, saved);
            }
        }

        return supplierMap;
    }

    private void seedRealWorldMedicines(Map<String, Category> categories, Map<String, Supplier> suppliers) {
        LocalDate today = LocalDate.now();

        // Helper supplier finder
        Supplier sunPharma = findSupplier(suppliers, "sun pharma");
        Supplier cipla = findSupplier(suppliers, "cipla");
        Supplier drReddy = findSupplier(suppliers, "dr. reddy");
        Supplier torrent = findSupplier(suppliers, "torrent");
        Supplier abbott = findSupplier(suppliers, "abbott");
        Supplier mankind = findSupplier(suppliers, "mankind");
        Supplier lupin = findSupplier(suppliers, "lupin");
        Supplier alkem = findSupplier(suppliers, "alkem");

        // Helper category finder
        Category analgesics = findCategory(categories, "analgesics");
        Category antibiotics = findCategory(categories, "antibiotics");
        Category cardio = findCategory(categories, "cardiovascular");
        Category diabetes = findCategory(categories, "antidiabetics");
        Category gastro = findCategory(categories, "gastrointestinal");
        Category respiratory = findCategory(categories, "respiratory");
        Category derma = findCategory(categories, "dermatologicals");
        Category vitamins = findCategory(categories, "vitamins");
        Category ent = findCategory(categories, "ophthalmic");
        Category surgical = findCategory(categories, "surgical");

        List<MedicineSeedDTO> catalog = List.of(
            // ── Analgesics & Antipyretics
            new MedicineSeedDTO("Dolo 650mg Tablets (Paracetamol)", "DOL-2025-019", 250, today.minusMonths(3), today.plusMonths(24), 30.50, analgesics, micropharma(sunPharma, cipla)),
            new MedicineSeedDTO("Crocin Advance 500mg (Fast Relief)", "CRC-2025-114", 180, today.minusMonths(4), today.plusMonths(20), 22.00, analgesics, micropharma(drReddy, abbott)),
            new MedicineSeedDTO("Combiflam Tablets (Ibuprofen + Paracetamol)", "CMB-2025-088", 120, today.minusMonths(2), today.plusMonths(18), 41.50, analgesics, micropharma(sanofiFallback(mankind), sunPharma)),
            new MedicineSeedDTO("Voveran 50mg Tablets (Diclofenac Sodium)", "VOV-2025-302", 8, today.minusMonths(6), today.plusDays(45), 58.00, analgesics, micropharma(torrent, cipla)), // Low stock & expiring soon
            new MedicineSeedDTO("Saridon Headache Relief Tablets", "SAR-2025-411", 300, today.minusMonths(5), today.plusMonths(22), 42.00, analgesics, micropharma(abbott, sunPharma)),
            new MedicineSeedDTO("Meftal-Spas Tablets (Mefenamic + Dicyclomine)", "MFT-2025-201", 90, today.minusMonths(3), today.plusMonths(16), 48.00, analgesics, micropharma(alkem, lupin)),

            // ── Antibiotics & Antimicrobials
            new MedicineSeedDTO("Augmentin 625 Duo Tablets (Amoxicillin + Clavulanate)", "AUG-2025-412", 140, today.minusMonths(2), today.plusMonths(18), 204.50, antibiotics, micropharma(sunPharma, cipla)),
            new MedicineSeedDTO("Azithral 500mg Tablets (Azithromycin)", "AZI-2025-103", 95, today.minusMonths(4), today.plusMonths(15), 118.00, antibiotics, micropharma(alkem, lupin)),
            new MedicineSeedDTO("Ciplox 500mg Tablets (Ciprofloxacin)", "CIP-2025-081", 6, today.minusMonths(8), today.plusDays(25), 44.00, antibiotics, micropharma(cipla, drReddy)), // Low stock & critical expiry
            new MedicineSeedDTO("Taxim-O 200mg Tablets (Cefixime)", "TAX-2025-722", 110, today.minusMonths(1), today.plusMonths(20), 105.00, antibiotics, micropharma(alkem, mankind)),
            new MedicineSeedDTO("Monocef-O 200mg Tablets", "MON-2025-601", 75, today.minusMonths(3), today.plusMonths(17), 165.00, antibiotics, micropharma(drReddy, torrent)),
            new MedicineSeedDTO("Metrogyl 400mg Tablets (Metronidazole)", "MET-2025-502", 200, today.minusMonths(5), today.plusMonths(24), 23.50, antibiotics, micropharma(mankind, abbott)),

            // ── Cardiovascular & Antihypertensives
            new MedicineSeedDTO("Telma 40mg Tablets (Telmisartan)", "TEL-2025-231", 160, today.minusMonths(2), today.plusMonths(22), 92.00, cardio, micropharma(sunPharma, torrent)),
            new MedicineSeedDTO("Amlong 5mg Tablets (Amlodipine)", "AML-2025-345", 130, today.minusMonths(4), today.plusMonths(20), 45.00, cardio, micropharma(drReddy, lupin)),
            new MedicineSeedDTO("Atorva 10mg Tablets (Atorvastatin)", "ATV-2025-881", 85, today.minusMonths(3), today.plusMonths(18), 110.00, cardio, micropharma(sunPharma, drReddy)),
            new MedicineSeedDTO("Concor 5mg Tablets (Bisoprolol)", "CNC-2025-199", 5, today.minusMonths(7), today.plusDays(60), 125.00, cardio, micropharma(abbott, cipla)), // Low stock
            new MedicineSeedDTO("Ecosprin 75mg Gastro-Resistant Tablets (Aspirin)", "ECO-2025-903", 220, today.minusMonths(2), today.plusMonths(24), 9.50, cardio, micropharma(sunPharma, torrent)),

            // ── Antidiabetics & Endocrine
            new MedicineSeedDTO("Glycomet-GP 2 Tablets (Metformin + Glimepiride)", "GLY-2025-094", 175, today.minusMonths(3), today.plusMonths(20), 135.00, diabetes, micropharma(sunPharma, torrent)),
            new MedicineSeedDTO("Januvia 100mg Tablets (Sitagliptin)", "JAN-2025-451", 45, today.minusMonths(2), today.plusMonths(16), 385.00, diabetes, micropharma(sunPharma, abbott)),
            new MedicineSeedDTO("Galvus Met 50/500mg Tablets", "GLV-2025-632", 60, today.minusMonths(4), today.plusMonths(18), 295.00, diabetes, micropharma(cipla, lupin)),
            new MedicineSeedDTO("Amaryl 1mg Tablets (Glimepiride)", "AMR-2025-714", 9, today.minusMonths(5), today.plusDays(70), 88.00, diabetes, micropharma(sanofiFallback(mankind), sunPharma)), // Low stock & expiring soon
            new MedicineSeedDTO("Thyronorm 50mcg Tablets (Levothyroxine Sodium)", "THY-2025-802", 150, today.minusMonths(2), today.plusMonths(18), 142.00, diabetes, micropharma(abbott, drReddy)),

            // ── Gastrointestinal & Antacids
            new MedicineSeedDTO("Pan 40mg Tablets (Pantoprazole)", "PAN-2025-108", 220, today.minusMonths(3), today.plusMonths(22), 85.00, gastro, micropharma(alkem, sunPharma)),
            new MedicineSeedDTO("Pantocid-DSR Capsules (Pantoprazole + Domperidone)", "PND-2025-241", 130, today.minusMonths(2), today.plusMonths(18), 145.00, gastro, micropharma(sunPharma, cipla)),
            new MedicineSeedDTO("Razo 20mg Tablets (Rabeprazole)", "RAZ-2025-333", 110, today.minusMonths(4), today.plusMonths(20), 98.00, gastro, micropharma(drReddy, lupin)),
            new MedicineSeedDTO("Omez 20mg Capsules (Omeprazole)", "OMZ-2025-512", 190, today.minusMonths(3), today.plusMonths(24), 62.00, gastro, micropharma(drReddy, torrent)),
            new MedicineSeedDTO("Gelusil MPS Antacid Syrup (200ml Mint)", "GEL-2025-621", 40, today.minusMonths(1), today.plusMonths(15), 112.00, gastro, micropharma(mankind, abbott)),
            new MedicineSeedDTO("Digene Chewable Tablets (Orange Flavour, Strip of 15)", "DIG-2025-780", 250, today.minusMonths(2), today.plusMonths(24), 26.00, gastro, micropharma(abbott, sunPharma)),
            new MedicineSeedDTO("Cremaffin Sugar-Free Mixed Fruit Laxative Syrup (225ml)", "CRM-2025-891", 35, today.minusMonths(3), today.plusMonths(14), 168.00, gastro, micropharma(abbott, cipla)),

            // ── Respiratory & Antihistamines
            new MedicineSeedDTO("Montair-LC Tablets (Montelukast + Levocetirizine)", "MNT-2025-419", 140, today.minusMonths(2), today.plusMonths(20), 180.00, respiratory, micropharma(cipla, sunPharma)),
            new MedicineSeedDTO("Allegra 120mg Tablets (Fexofenadine Hydrochloride)", "ALG-2025-703", 90, today.minusMonths(3), today.plusMonths(18), 195.00, respiratory, micropharma(sanofiFallback(mankind), abbott)),
            new MedicineSeedDTO("Cetzine 10mg Tablets (Cetirizine Hydrochloride)", "CTZ-2025-338", 180, today.minusMonths(4), today.plusMonths(22), 21.00, respiratory, micropharma(drReddy, cipla)),
            new MedicineSeedDTO("Ascoril-D Plus Cough Syrup (100ml)", "ASC-2025-912", 45, today.minusMonths(2), today.plusMonths(16), 115.00, respiratory, micropharma(glenmarkFallback(lupin), alkem)),
            new MedicineSeedDTO("Benadryl Cough Formula Syrup (100ml)", "BND-2025-104", 50, today.minusMonths(3), today.plusMonths(15), 125.00, respiratory, micropharma(abbott, mankind)),
            new MedicineSeedDTO("Otrivin Adult 0.1% Nasal Decongestant Spray (10ml)", "OTR-2025-667", 65, today.minusMonths(2), today.plusMonths(18), 108.00, ent, micropharma(gskFallback(sunPharma), cipla)),
            new MedicineSeedDTO("Asthalin 100mcg Inhaler (Salbutamol 200 Metered Doses)", "AST-2025-442", 30, today.minusMonths(1), today.plusMonths(14), 162.00, respiratory, micropharma(cipla, lupin)),

            // ── Vitamins, Minerals & Supplements
            new MedicineSeedDTO("Becosules Z Capsules (B-Complex + Vitamin C + Zinc)", "BEC-2025-331", 280, today.minusMonths(3), today.plusMonths(24), 48.00, vitamins, micropharma(sunPharma, mankind)),
            new MedicineSeedDTO("Limcee 500mg Chewable Vitamin C Tablets (Strip of 15)", "LIM-2025-554", 320, today.minusMonths(2), today.plusMonths(22), 24.50, vitamins, micropharma(abbott, cipla)),
            new MedicineSeedDTO("Shelcal 500mg Tablets (Calcium + Vitamin D3)", "SHL-2025-789", 190, today.minusMonths(4), today.plusMonths(20), 128.00, vitamins, micropharma(torrent, sunPharma)),
            new MedicineSeedDTO("Neurobion Forte Tablets (Vitamin B12 + B6 + B1)", "NEU-2025-992", 210, today.minusMonths(3), today.plusMonths(24), 42.00, vitamins, micropharma(mankind, abbott)),
            new MedicineSeedDTO("Zincovit Multivitamin & Mineral Tablets", "ZNC-2025-121", 160, today.minusMonths(2), today.plusMonths(18), 110.00, vitamins, micropharma(alkem, torrent)),
            new MedicineSeedDTO("Evion 400mg Capsules (Vitamin E for Skin & Muscle)", "EVN-2025-873", 240, today.minusMonths(3), today.plusMonths(22), 38.00, vitamins, micropharma(mankind, sunPharma)),
            new MedicineSeedDTO("Supradyn Daily Multivitamin Tablets (Strip of 15)", "SUP-2025-650", 110, today.minusMonths(2), today.plusMonths(18), 64.00, vitamins, micropharma(bayerFallback(abbott), cipla)),

            // ── Dermatologicals & Antiseptics
            new MedicineSeedDTO("Betadine 10% Antiseptic Ointment (20g Tube)", "BET-2025-502", 80, today.minusMonths(3), today.plusMonths(20), 78.00, derma, micropharma(winmedFallback(mankind), cipla)),
            new MedicineSeedDTO("Soframycin Skin Antibiotic Cream (30g)", "SOF-2025-711", 60, today.minusMonths(4), today.plusMonths(18), 62.00, derma, micropharma(sanofiFallback(mankind), sunPharma)),
            new MedicineSeedDTO("Volini Pain Relief Gel (50g Tube)", "VOL-2025-420", 75, today.minusMonths(2), today.plusMonths(22), 145.00, derma, micropharma(sunPharma, torrent)),
            new MedicineSeedDTO("Moov Pain Relief Spray (80g Canister)", "MOV-2025-908", 45, today.minusMonths(3), today.plusMonths(16), 185.00, derma, micropharma(reckittFallback(abbott), sunPharma)),
            new MedicineSeedDTO("Dettol Antiseptic Liquid (250ml Bottle)", "DET-2025-334", 90, today.minusMonths(2), today.plusMonths(24), 132.00, derma, micropharma(reckittFallback(abbott), mankind)),
            new MedicineSeedDTO("Savlon Antiseptic Disinfectant Liquid (200ml)", "SAV-2025-562", 70, today.minusMonths(3), today.plusMonths(24), 105.00, derma, micropharma(itcFallback(mankind), cipla)),

            // ── First Aid, Diagnostic & Surgical Supplies
            new MedicineSeedDTO("Band-Aid Washproof Strips (Pack of 50 Medicated Strips)", "BND-2025-501", 120, today.minusMonths(2), today.plusMonths(36), 95.00, surgical, micropharma(jjFallback(abbott), mankind)),
            new MedicineSeedDTO("Hansaplast Elastic Cotton Crepe Bandage (10cm x 4m)", "HAN-2025-882", 55, today.minusMonths(4), today.plusMonths(36), 180.00, surgical, micropharma(beiersdorfFallback(sunPharma), cipla)),
            new MedicineSeedDTO("Accu-Chek Active Blood Glucose Test Strips (Pack of 50)", "ACC-2025-774", 25, today.minusMonths(1), today.plusMonths(14), 840.00, surgical, micropharma(rocheFallback(abbott), drReddy)),
            new MedicineSeedDTO("Omron HEM-7120 Digital Automatic BP Monitor", "OMR-2025-101", 12, today.minusMonths(1), today.plusMonths(60), 1950.00, surgical, micropharma(omronFallback(sunPharma), cipla)),
            new MedicineSeedDTO("Dr. Trust Infrared Forehead & Ear Thermometer", "DRT-2025-309", 18, today.minusMonths(2), today.plusMonths(48), 1250.00, surgical, micropharma(nurecaFallback(drReddy), mankind)),
            new MedicineSeedDTO("BD Ultra-Fine Insulin Syringes 31G (Pack of 10)", "BDU-2025-619", 40, today.minusMonths(3), today.plusMonths(30), 140.00, surgical, micropharma(bdFallback(cipla), abbott)),
            new MedicineSeedDTO("Sterile Cotton Gauze Swabs 7.5cm x 7.5cm (Pack of 100)", "GAU-2025-990", 85, today.minusMonths(2), today.plusMonths(36), 120.00, surgical, micropharma(sunPharma, mankind))
        );

        // Save each item if not already registered by name
        int addedCount = 0;
        for (MedicineSeedDTO item : catalog) {
            Optional<Medicine> existing = medicineRepository.findFirstByNameIgnoreCase(item.name);
            if (existing.isEmpty()) {
                Medicine m = new Medicine();
                m.setName(item.name);
                m.setBatchNumber(item.batchNumber);
                m.setQuantity(item.quantity);
                m.setManufacturingDate(item.mfgDate);
                m.setExpiryDate(item.expDate);
                m.setPrice(item.price);
                m.setCategory(item.category);
                m.setSupplier(item.supplier);
                medicineRepository.save(m);
                addedCount++;
            }
        }

        logger.info("✓ [DataLoader] Seeded {} new real-world pharmacy medicines & supplies.", addedCount);
    }

    /* ── Helper Utilities ─────────────────────────────────────────────────── */
    private static Supplier micropharma(Supplier preferred, Supplier fallback) {
        return preferred != null ? preferred : fallback;
    }
    private static Supplier sanofiFallback(Supplier fallback) { return fallback; }
    private static Supplier glenmarkFallback(Supplier fallback) { return fallback; }
    private static Supplier gskFallback(Supplier fallback) { return fallback; }
    private static Supplier bayerFallback(Supplier fallback) { return fallback; }
    private static Supplier winmedFallback(Supplier fallback) { return fallback; }
    private static Supplier reckittFallback(Supplier fallback) { return fallback; }
    private static Supplier itcFallback(Supplier fallback) { return fallback; }
    private static Supplier jjFallback(Supplier fallback) { return fallback; }
    private static Supplier beiersdorfFallback(Supplier fallback) { return fallback; }
    private static Supplier rocheFallback(Supplier fallback) { return fallback; }
    private static Supplier omronFallback(Supplier fallback) { return fallback; }
    private static Supplier nurecaFallback(Supplier fallback) { return fallback; }
    private static Supplier bdFallback(Supplier fallback) { return fallback; }

    private Supplier findSupplier(Map<String, Supplier> map, String query) {
        for (Map.Entry<String, Supplier> entry : map.entrySet()) {
            if (entry.getKey().contains(query.toLowerCase())) {
                return entry.getValue();
            }
        }
        return map.values().stream().findFirst().orElse(null);
    }

    private Category findCategory(Map<String, Category> map, String query) {
        for (Map.Entry<String, Category> entry : map.entrySet()) {
            if (entry.getKey().contains(query.toLowerCase())) {
                return entry.getValue();
            }
        }
        return map.values().stream().findFirst().orElse(null);
    }

    private static class MedicineSeedDTO {
        String name;
        String batchNumber;
        int quantity;
        LocalDate mfgDate;
        LocalDate expDate;
        double price;
        Category category;
        Supplier supplier;

        MedicineSeedDTO(String name, String batchNumber, int quantity, LocalDate mfgDate, LocalDate expDate, double price, Category category, Supplier supplier) {
            this.name = name;
            this.batchNumber = batchNumber;
            this.quantity = quantity;
            this.mfgDate = mfgDate;
            this.expDate = expDate;
            this.price = price;
            this.category = category;
            this.supplier = supplier;
        }
    }
}
