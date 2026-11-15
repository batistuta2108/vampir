package com.example.data.templates

import com.example.data.model.ProjectCategory

data class WebTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: ProjectCategory,
    val badge: String,
    val htmlCode: String,
    val cssCode: String,
    val jsCode: String
)

object WebTemplates {
    val all: List<WebTemplate> = listOf(
        WebTemplate(
            id = "tpl_saas_ai",
            title = "NovaFlow SaaS Pro",
            subtitle = "Landing SaaS complète avec simulateur ROI, tarifs mensuels/annuels et FAQ interactive",
            category = ProjectCategory.SAAS,
            badge = "Illimité Pro",
            htmlCode = """
                <header class="nav">
                  <div class="container nav-inner">
                    <div class="brand"><span class="dot"></span>NovaFlow<span>Pro</span></div>
                    <nav class="links">
                      <a href="#features">Produit</a>
                      <a href="#roi">Simulateur</a>
                      <a href="#pricing">Tarifs</a>
                    </nav>
                    <button class="btn-primary" onclick="openDemo()">Accès Illimité</button>
                  </div>
                </header>
                <main>
                  <section class="hero container">
                    <span class="pill">Plateforme Cloud Next-Gen • Tout inclus</span>
                    <h1>Automatisez vos workflows à la vitesse de la lumière.</h1>
                    <p>Déployez des pipelines intelligents, connectez vos équipes et pilotez votre croissance en temps réel depuis une interface unifiée.</p>
                    <div class="hero-actions">
                      <button class="btn-primary btn-lg" onclick="openDemo()">Démarrer gratuitement</button>
                      <button class="btn-ghost btn-lg" onclick="document.getElementById('roi').scrollIntoView({behavior:'smooth'})">Calculer mon ROI</button>
                    </div>
                    <div class="stats-row">
                      <div class="stat"><strong>99.98%</strong><span>Disponibilité SLA</span></div>
                      <div class="stat"><strong>14x</strong><span>Déploiements plus rapides</span></div>
                      <div class="stat"><strong>Illimité</strong><span>Projets & Collaborateurs</span></div>
                    </div>
                  </section>

                  <section id="features" class="container section">
                    <h2>Architecture complète prête pour la production</h2>
                    <div class="bento">
                      <article class="card"><h3>⚡ IA Temps Réel</h3><p>Génération automatique de rapports, détection d'anomalies et orchestration multi-agents.</p></article>
                      <article class="card"><h3>🔒 Sécurité Zero-Trust</h3><p>Chiffrement de bout en bout, SSO entreprise et journaux d'audit détaillés.</p></article>
                      <article class="card"><h3>📊 Analytics Live</h3><p>Tableaux de bord interactifs avec métriques à la milliseconde et alertes intelligentes.</p></article>
                    </div>
                  </section>

                  <section id="roi" class="container section roi-box">
                    <div>
                      <h2>Estimez votre gain mensuel</h2>
                      <p>Ajustez la taille de votre équipe pour simuler l'économie générée.</p>
                      <label for="teamRange">Taille de l'équipe : <strong id="teamCount">12</strong> membres</label>
                      <input id="teamRange" type="range" min="1" max="100" value="12" oninput="updateRoi(this.value)" />
                    </div>
                    <div class="roi-result">
                      <span>Économie estimée / mois</span>
                      <strong id="roiAmount">4 320 €</strong>
                      <small id="hoursSaved">144 heures économisées</small>
                    </div>
                  </section>

                  <section id="pricing" class="container section">
                    <div class="pricing-head">
                      <h2>Plans transparents</h2>
                      <button class="toggle-btn" id="billingToggle" onclick="toggleBilling()">Facturation : Annuelle (-25%)</button>
                    </div>
                    <div class="pricing-grid">
                      <div class="price-card">
                        <h3>Starter</h3>
                        <div class="price"><span data-m="29" data-y="22">22</span> €<small>/mois</small></div>
                        <ul><li>✓ Projets illimités</li><li>✓ Aperçu responsive</li><li>✓ Export HTML complet</li></ul>
                        <button class="btn-ghost" onclick="openDemo()">Choisir Starter</button>
                      </div>
                      <div class="price-card featured">
                        <span class="tag">POPULAIRE</span>
                        <h3>Studio Pro Illimité</h3>
                        <div class="price"><span data-m="79" data-y="59">59</span> €<small>/mois</small></div>
                        <ul><li>✓ Tout illimité sans quota</li><li>✓ Historique & rollback complet</li><li>✓ Support prioritaire 24/7</li></ul>
                        <button class="btn-primary" onclick="openDemo()">Activer Studio Pro</button>
                      </div>
                    </div>
                  </section>
                </main>
                <div id="modal" class="modal hidden">
                  <div class="modal-card">
                    <h3>Espace Pro Activé</h3>
                    <p>Votre environnement NovaFlow Pro est prêt avec accès illimité à tous les modules.</p>
                    <button class="btn-primary" onclick="closeDemo()">Fermer</button>
                  </div>
                </div>
            """.trimIndent(),
            cssCode = """
                :root { --bg:#0b0e14; --card:#141a26; --border:#242e42; --primary:#7c5cff; --accent:#2ce5b5; --text:#f4f6fb; --muted:#9aa7c0; }
                * { box-sizing:border-box; margin:0; padding:0; }
                body { font-family:system-ui,-apple-system,sans-serif; background:var(--bg); color:var(--text); line-height:1.6; }
                .container { max-width:1080px; margin:0 auto; padding:0 20px; }
                .nav { position:sticky; top:0; background:rgba(11,14,20,0.88); backdrop-filter:blur(12px); border-bottom:1px solid var(--border); z-index:20; }
                .nav-inner { display:flex; align-items:center; justify-content:space-between; height:68px; gap:12px; }
                .brand { font-weight:800; font-size:1.2rem; display:flex; align-items:center; gap:8px; }
                .brand span:last-child { color:var(--primary); }
                .dot { width:10px; height:10px; border-radius:50%; background:linear-gradient(135deg,#7c5cff,#ff5c8d); }
                .links { display:flex; gap:20px; }
                .links a { color:var(--muted); text-decoration:none; font-size:0.95rem; }
                .btn-primary { background:linear-gradient(135deg,#7c5cff,#ff5c8d); color:#fff; border:none; padding:10px 18px; border-radius:10px; font-weight:600; cursor:pointer; }
                .btn-ghost { background:var(--card); color:var(--text); border:1px solid var(--border); padding:10px 18px; border-radius:10px; font-weight:600; cursor:pointer; }
                .btn-lg { padding:14px 24px; font-size:1rem; }
                .hero { padding:72px 20px 48px; text-align:center; }
                .pill { display:inline-block; padding:6px 14px; border-radius:999px; background:rgba(124,92,255,0.16); color:#c5b6ff; font-size:0.85rem; margin-bottom:18px; }
                .hero h1 { font-size:clamp(2rem,5vw,3.4rem); line-height:1.12; margin-bottom:16px; }
                .hero p { color:var(--muted); max-width:640px; margin:0 auto 28px; }
                .hero-actions { display:flex; justify-content:center; gap:12px; flex-wrap:wrap; margin-bottom:44px; }
                .stats-row { display:grid; grid-template-columns:repeat(auto-fit,minmax(160px,1fr)); gap:16px; background:var(--card); border:1px solid var(--border); border-radius:18px; padding:20px; }
                .stat strong { display:block; font-size:1.5rem; color:var(--accent); }
                .stat span { font-size:0.85rem; color:var(--muted); }
                .section { padding:52px 20px; }
                .section h2 { font-size:1.8rem; margin-bottom:24px; }
                .bento { display:grid; grid-template-columns:repeat(auto-fit,minmax(240px,1fr)); gap:16px; }
                .card { background:var(--card); border:1px solid var(--border); border-radius:16px; padding:22px; }
                .card h3 { margin-bottom:8px; }
                .card p { color:var(--muted); font-size:0.95rem; }
                .roi-box { display:grid; grid-template-columns:repeat(auto-fit,minmax(260px,1fr)); gap:24px; background:linear-gradient(135deg,#151c2c,#1a1730); border:1px solid var(--border); border-radius:20px; padding:28px; align-items:center; }
                .roi-box input { width:100%; margin-top:12px; accent-color:var(--primary); }
                .roi-result { background:rgba(11,14,20,0.6); border:1px solid var(--border); border-radius:16px; padding:22px; text-align:center; }
                .roi-result strong { display:block; font-size:2.2rem; color:var(--accent); margin:6px 0; }
                .pricing-head { display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; margin-bottom:24px; }
                .toggle-btn { background:var(--card); color:var(--accent); border:1px solid var(--border); padding:8px 14px; border-radius:999px; cursor:pointer; font-weight:600; }
                .pricing-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(260px,1fr)); gap:18px; }
                .price-card { background:var(--card); border:1px solid var(--border); border-radius:18px; padding:24px; position:relative; }
                .price-card.featured { border-color:var(--primary); box-shadow:0 0 32px rgba(124,92,255,0.18); }
                .tag { position:absolute; top:14px; right:14px; background:var(--primary); color:#fff; font-size:0.7rem; padding:4px 8px; border-radius:6px; font-weight:700; }
                .price { font-size:2.2rem; font-weight:800; margin:12px 0; }
                .price small { font-size:0.95rem; color:var(--muted); }
                .price-card ul { list-style:none; margin-bottom:20px; color:var(--muted); }
                .price-card li { margin:8px 0; }
                .modal { position:fixed; inset:0; background:rgba(0,0,0,0.7); display:flex; align-items:center; justify-content:center; padding:20px; z-index:50; }
                .hidden { display:none; }
                .modal-card { background:var(--card); border:1px solid var(--primary); border-radius:18px; padding:28px; max-width:400px; text-align:center; }
                .modal-card p { color:var(--muted); margin:12px 0 20px; }
                @media(max-width:600px){ .links{display:none;} }
            """.trimIndent(),
            jsCode = """
                let yearly = true;
                function updateRoi(val) {
                  document.getElementById('teamCount').textContent = val;
                  document.getElementById('roiAmount').textContent = (val * 360).toLocaleString('fr-FR') + ' €';
                  document.getElementById('hoursSaved').textContent = (val * 12) + ' heures économisées';
                }
                function toggleBilling() {
                  yearly = !yearly;
                  document.getElementById('billingToggle').textContent = yearly ? 'Facturation : Annuelle (-25%)' : 'Facturation : Mensuelle';
                  document.querySelectorAll('.price span[data-m]').forEach(el => {
                    el.textContent = yearly ? el.dataset.y : el.dataset.m;
                  });
                }
                function openDemo() { document.getElementById('modal').classList.remove('hidden'); }
                function closeDemo() { document.getElementById('modal').classList.add('hidden'); }
            """.trimIndent()
        ),
        WebTemplate(
            id = "tpl_ecommerce",
            title = "Maison Aurélia Boutique",
            subtitle = "Boutique e-commerce complète avec filtres catégories et panier dynamique en temps réel",
            category = ProjectCategory.ECOMMERCE,
            badge = "Panier Live",
            htmlCode = """
                <header class="topbar">
                  <div class="wrap topbar-in">
                    <div class="logo">MAISON AURÉLIA</div>
                    <div class="filters">
                      <button class="chip active" onclick="filterProducts('all', this)">Tous</button>
                      <button class="chip" onclick="filterProducts('montres', this)">Horlogerie</button>
                      <button class="chip" onclick="filterProducts('audio', this)">Audio</button>
                      <button class="chip" onclick="filterProducts('cuir', this)">Maroquinerie</button>
                    </div>
                    <button class="cart-btn" onclick="toggleCart()">Panier (<span id="cartCount">0</span>)</button>
                  </div>
                </header>
                <section class="banner">
                  <div class="wrap">
                    <p class="eyebrow">COLLECTION STUDIO 2026</p>
                    <h1>Design intemporel, précision artisanale.</h1>
                    <p>Livraison express offerte et garantie 5 ans sur toute la gamme.</p>
                  </div>
                </section>
                <main class="wrap grid" id="productGrid"></main>
                <aside id="cartDrawer" class="drawer hidden">
                  <div class="drawer-head">
                    <h3>Votre Panier</h3>
                    <button onclick="toggleCart()">✕</button>
                  </div>
                  <div id="cartItems" class="drawer-body"></div>
                  <div class="drawer-foot">
                    <div class="total-row"><span>Total</span><strong id="cartTotal">0 €</strong></div>
                    <button class="checkout-btn" onclick="checkout()">Commander maintenant</button>
                  </div>
                </aside>
            """.trimIndent(),
            cssCode = """
                * { box-sizing:border-box; margin:0; padding:0; }
                body { font-family:system-ui,sans-serif; background:#f8f7f4; color:#18181b; }
                .wrap { max-width:1100px; margin:0 auto; padding:0 18px; }
                .topbar { background:#fff; border-bottom:1px solid #e7e5e0; position:sticky; top:0; z-index:20; }
                .topbar-in { display:flex; align-items:center; justify-content:space-between; gap:12px; min-height:66px; flex-wrap:wrap; padding:10px 0; }
                .logo { font-weight:800; letter-spacing:0.12em; }
                .filters { display:flex; gap:8px; flex-wrap:wrap; }
                .chip { border:1px solid #ddd; background:#fff; padding:6px 14px; border-radius:999px; cursor:pointer; font-size:0.85rem; }
                .chip.active { background:#18181b; color:#fff; border-color:#18181b; }
                .cart-btn { background:#7c5cff; color:#fff; border:none; padding:9px 16px; border-radius:10px; font-weight:600; cursor:pointer; }
                .banner { background:linear-gradient(120deg,#18181b,#2d2547); color:#fff; padding:54px 0; text-align:center; }
                .eyebrow { color:#a58eff; font-size:0.8rem; letter-spacing:0.15em; margin-bottom:10px; }
                .banner h1 { font-size:clamp(1.8rem,4vw,2.8rem); margin-bottom:10px; }
                .grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(240px,1fr)); gap:18px; padding:34px 18px; }
                .product { background:#fff; border:1px solid #eceae4; border-radius:16px; overflow:hidden; display:flex; flex-direction:column; }
                .thumb { height:160px; display:flex; align-items:center; justify-content:center; font-size:3rem; background:linear-gradient(135deg,#f1effb,#e8f7f3); }
                .p-body { padding:16px; flex:1; display:flex; flex-direction:column; justify-content:space-between; gap:10px; }
                .p-cat { font-size:0.75rem; text-transform:uppercase; color:#71717a; }
                .p-foot { display:flex; justify-content:space-between; align-items:center; }
                .add-btn { background:#18181b; color:#fff; border:none; padding:8px 14px; border-radius:8px; cursor:pointer; font-weight:600; }
                .drawer { position:fixed; top:0; right:0; bottom:0; width:min(360px,92vw); background:#fff; box-shadow:-8px 0 30px rgba(0,0,0,0.18); z-index:40; display:flex; flex-direction:column; }
                .hidden { display:none; }
                .drawer-head, .drawer-foot { padding:18px; border-bottom:1px solid #eee; display:flex; justify-content:space-between; align-items:center; }
                .drawer-foot { border-top:1px solid #eee; border-bottom:none; flex-direction:column; gap:12px; align-items:stretch; }
                .drawer-body { flex:1; overflow:auto; padding:16px; display:flex; flex-direction:column; gap:12px; }
                .cart-item { display:flex; justify-content:space-between; align-items:center; background:#f7f7f7; padding:10px 12px; border-radius:10px; }
                .total-row { display:flex; justify-content:space-between; font-size:1.1rem; }
                .checkout-btn { background:#7c5cff; color:#fff; border:none; padding:12px; border-radius:10px; font-weight:700; cursor:pointer; }
            """.trimIndent(),
            jsCode = """
                const products = [
                  { id:1, name:'Chrono Solaire Noir', cat:'montres', price:490, icon:'⌚' },
                  { id:2, name:'Casque Studio Pro', cat:'audio', price:340, icon:'🎧' },
                  { id:3, name:'Sacoche Cuir Milan', cat:'cuir', price:280, icon:'👜' },
                  { id:4, name:'Enceinte Cristal Hi-Fi', cat:'audio', price:520, icon:'🔊' },
                  { id:5, name:'Montre Automatique Or', cat:'montres', price:890, icon:'🕰️' },
                  { id:6, name:'Portefeuille Atelier', cat:'cuir', price:145, icon:'💼' }
                ];
                let cart = [];
                let currentFilter = 'all';
                function renderProducts() {
                  const grid = document.getElementById('productGrid');
                  const items = currentFilter === 'all' ? products : products.filter(p => p.cat === currentFilter);
                  grid.innerHTML = items.map(p => `
                    <article class="product">
                      <div class="thumb">${'$'}{p.icon}</div>
                      <div class="p-body">
                        <div><div class="p-cat">${'$'}{p.cat}</div><h3>${'$'}{p.name}</h3></div>
                        <div class="p-foot"><strong>${'$'}{p.price} €</strong><button class="add-btn" onclick="addToCart(${'$'}{p.id})">Ajouter</button></div>
                      </div>
                    </article>
                  `).join('');
                }
                function filterProducts(cat, btn) {
                  currentFilter = cat;
                  document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
                  btn.classList.add('active');
                  renderProducts();
                }
                function addToCart(id) {
                  const found = cart.find(i => i.id === id);
                  if (found) found.qty++; else cart.push({...products.find(p => p.id === id), qty:1});
                  renderCart();
                }
                function removeOne(id) {
                  const idx = cart.findIndex(i => i.id === id);
                  if (idx > -1) {
                    if (cart[idx].qty > 1) cart[idx].qty--; else cart.splice(idx, 1);
                  }
                  renderCart();
                }
                function renderCart() {
                  document.getElementById('cartCount').textContent = cart.reduce((s, i) => s + i.qty, 0);
                  document.getElementById('cartTotal').textContent = cart.reduce((s, i) => s + i.price * i.qty, 0) + ' €';
                  document.getElementById('cartItems').innerHTML = cart.length ? cart.map(i => `
                    <div class="cart-item">
                      <div><strong>${'$'}{i.name}</strong><br><small>${'$'}{i.qty} x ${'$'}{i.price} €</small></div>
                      <button onclick="removeOne(${'$'}{i.id})">−1</button>
                    </div>
                  `).join('') : '<p style="color:#777">Votre panier est vide.</p>';
                }
                function toggleCart() { document.getElementById('cartDrawer').classList.toggle('hidden'); }
                function checkout() {
                  if (!cart.length) return alert('Ajoutez au moins un article.');
                  alert('Commande confirmée ! Merci pour votre achat chez Maison Aurélia.');
                  cart = []; renderCart(); toggleCart();
                }
                renderProducts(); renderCart();
            """.trimIndent()
        ),
        WebTemplate(
            id = "tpl_dashboard",
            title = "PulseMetrics Analytics",
            subtitle = "Dashboard SaaS temps réel avec graphiques SVG interactifs, KPIs et tableau filtrable",
            category = ProjectCategory.DASHBOARD,
            badge = "Data Live",
            htmlCode = """
                <div class="app">
                  <header class="head">
                    <div><h1>PulseMetrics Pro</h1><p>Vue analytique consolidée en temps réel</p></div>
                    <select id="periodSelect" onchange="updatePeriod(this.value)">
                      <option value="7j">7 derniers jours</option>
                      <option value="30j" selected>30 derniers jours</option>
                      <option value="90j">90 derniers jours</option>
                    </select>
                  </header>
                  <section class="kpis">
                    <div class="kpi"><span>Revenu MRR</span><strong id="kpiMrr">48 920 €</strong><em class="up">+18.4%</em></div>
                    <div class="kpi"><span>Utilisateurs Actifs</span><strong id="kpiUsers">12 480</strong><em class="up">+9.2%</em></div>
                    <div class="kpi"><span>Conversion</span><strong id="kpiConv">6.84%</strong><em class="up">+1.1%</em></div>
                    <div class="kpi"><span>Temps Réponse</span><strong id="kpiLatency">42 ms</strong><em class="up">Optimal</em></div>
                  </section>
                  <section class="panel">
                    <h2>Croissance des revenus</h2>
                    <div id="chartBars" class="bars"></div>
                  </section>
                  <section class="panel">
                    <div class="panel-head">
                      <h2>Transactions récentes</h2>
                      <input type="search" placeholder="Filtrer un client..." oninput="filterRows(this.value)" />
                    </div>
                    <table>
                      <thead><tr><th>Client</th><th>Plan</th><th>Montant</th><th>Statut</th></tr></thead>
                      <tbody id="txBody"></tbody>
                    </table>
                  </section>
                </div>
            """.trimIndent(),
            cssCode = """
                * { box-sizing:border-box; margin:0; padding:0; }
                body { font-family:system-ui,sans-serif; background:#090d16; color:#eef2ff; padding:20px; }
                .app { max-width:1040px; margin:0 auto; display:flex; flex-direction:column; gap:18px; }
                .head { display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; }
                .head p { color:#8b9bb8; font-size:0.9rem; }
                select, input { background:#131b2b; color:#fff; border:1px solid #26324b; padding:9px 14px; border-radius:10px; }
                .kpis { display:grid; grid-template-columns:repeat(auto-fit,minmax(190px,1fr)); gap:14px; }
                .kpi { background:#131b2b; border:1px solid #232e47; border-radius:16px; padding:18px; }
                .kpi span { color:#8b9bb8; font-size:0.85rem; }
                .kpi strong { display:block; font-size:1.7rem; margin:6px 0; }
                .up { color:#2ce5b5; font-style:normal; font-size:0.85rem; font-weight:600; }
                .panel { background:#131b2b; border:1px solid #232e47; border-radius:18px; padding:20px; overflow:auto; }
                .panel h2 { font-size:1.1rem; margin-bottom:16px; }
                .panel-head { display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap; margin-bottom:14px; }
                .bars { display:flex; align-items:flex-end; gap:10px; height:160px; padding-top:12px; }
                .bar-wrap { flex:1; display:flex; flex-direction:column; align-items:center; gap:6px; height:100%; justify-content:flex-end; }
                .bar { width:100%; border-radius:8px 8px 2px 2px; background:linear-gradient(180deg,#7c5cff,#2ce5b5); transition:height .3s; }
                .bar-wrap small { color:#8b9bb8; font-size:0.75rem; }
                table { width:100%; border-collapse:collapse; }
                th, td { text-align:left; padding:12px 8px; border-bottom:1px solid #1f293d; font-size:0.92rem; }
                th { color:#8b9bb8; font-weight:600; }
                .badge { padding:4px 10px; border-radius:999px; font-size:0.78rem; background:rgba(44,229,181,0.16); color:#2ce5b5; }
            """.trimIndent(),
            jsCode = """
                const datasets = {
                  '7j':  { mrr:'12 400 €', users:'4 120', conv:'5.9%', bars:[42,58,51,70,68,84,92], labels:['Lun','Mar','Mer','Jeu','Ven','Sam','Dim'] },
                  '30j': { mrr:'48 920 €', users:'12 480', conv:'6.84%', bars:[45,55,62,74,69,88,96], labels:['S1','S2','S3','S4','S5','S6','S7'] },
                  '90j': { mrr:'142 600 €', users:'31 900', conv:'7.42%', bars:[38,48,60,72,81,90,100], labels:['J15','J30','J45','J60','J75','J85','J90'] }
                };
                const rows = [
                  { client:'Atelier Lumière', plan:'Studio Pro', amount:'590 €', status:'Payé' },
                  { client:'Kinetix Lab', plan:'Enterprise', amount:'1 450 €', status:'Payé' },
                  { client:'Nordic Cloud', plan:'Studio Pro', amount:'590 €', status:'Payé' },
                  { client:'Velvet Commerce', plan:'Starter', amount:'190 €', status:'Payé' }
                ];
                function updatePeriod(p) {
                  const d = datasets[p];
                  document.getElementById('kpiMrr').textContent = d.mrr;
                  document.getElementById('kpiUsers').textContent = d.users;
                  document.getElementById('kpiConv').textContent = d.conv;
                  document.getElementById('chartBars').innerHTML = d.bars.map((v, i) => `
                    <div class="bar-wrap"><div class="bar" style="height:${'$'}{v}%"></div><small>${'$'}{d.labels[i]}</small></div>
                  `).join('');
                }
                function filterRows(q) {
                  const filtered = rows.filter(r => r.client.toLowerCase().includes(q.toLowerCase()));
                  document.getElementById('txBody').innerHTML = filtered.map(r => `
                    <tr><td>${'$'}{r.client}</td><td>${'$'}{r.plan}</td><td>${'$'}{r.amount}</td><td><span class="badge">${'$'}{r.status}</span></td></tr>
                  `).join('');
                }
                updatePeriod('30j');
                filterRows('');
            """.trimIndent()
        ),
        WebTemplate(
            id = "tpl_portfolio",
            title = "Studio Lumière Portfolio",
            subtitle = "Portfolio créatif éditorial avec galerie filtrable et formulaire de contact interactif",
            category = ProjectCategory.PORTFOLIO,
            badge = "Design Éditorial",
            htmlCode = """
                <header class="hdr">
                  <div class="ct hdr-in">
                    <strong>LUMIÈRE.STUDIO</strong>
                    <a href="#contact" class="cta">Démarrer un projet</a>
                  </div>
                </header>
                <section class="hero ct">
                  <h1>Nous sculptons des marques digitales mémorables.</h1>
                  <p>Direction artistique, design systems et expériences web haute couture.</p>
                </section>
                <section class="ct works">
                  <article class="work"><span>01 / FINTECH</span><h3>Helios Banking</h3><p>Refonte complète de l'application mobile et design system multi-plateforme.</p></article>
                  <article class="work"><span>02 / LUXE</span><h3>Maison Solène</h3><p>Expérience e-commerce immersive et storytelling 3D temps réel.</p></article>
                  <article class="work"><span>03 / IA</span><h3>Aether Labs</h3><p>Identité visuelle et interface web pour laboratoire d'intelligence artificielle.</p></article>
                </section>
                <section id="contact" class="ct contact-box">
                  <h2>Parlons de votre vision</h2>
                  <form onsubmit="sendBrief(event)">
                    <input id="nameInput" required placeholder="Votre nom" />
                    <input id="emailInput" type="email" required placeholder="Votre email professionnel" />
                    <textarea id="msgInput" rows="3" required placeholder="Décrivez votre projet..."></textarea>
                    <button type="submit">Envoyer le brief</button>
                  </form>
                  <p id="formFeedback" class="feedback"></p>
                </section>
            """.trimIndent(),
            cssCode = """
                * { box-sizing:border-box; margin:0; padding:0; }
                body { font-family:system-ui,sans-serif; background:#101014; color:#f5f3ef; line-height:1.5; }
                .ct { max-width:960px; margin:0 auto; padding:0 20px; }
                .hdr { border-bottom:1px solid #262630; }
                .hdr-in { display:flex; justify-content:space-between; align-items:center; height:68px; }
                .cta { color:#101014; background:#f5f3ef; text-decoration:none; padding:9px 16px; border-radius:999px; font-weight:700; font-size:0.9rem; }
                .hero { padding:64px 20px 40px; }
                .hero h1 { font-size:clamp(2rem,5vw,3.5rem); line-height:1.08; margin-bottom:14px; }
                .hero p { color:#a0a0b0; font-size:1.1rem; }
                .works { display:grid; grid-template-columns:repeat(auto-fit,minmax(250px,1fr)); gap:16px; padding:24px 20px; }
                .work { background:#181820; border:1px solid #2a2a36; border-radius:18px; padding:24px; }
                .work span { color:#ff5c8d; font-size:0.78rem; font-weight:700; letter-spacing:0.1em; }
                .work h3 { margin:10px 0 8px; font-size:1.3rem; }
                .work p { color:#a0a0b0; font-size:0.95rem; }
                .contact-box { margin:40px auto 60px; background:#181820; border:1px solid #2a2a36; border-radius:20px; padding:28px; }
                .contact-box h2 { margin-bottom:16px; }
                form { display:flex; flex-direction:column; gap:12px; }
                input, textarea { background:#101014; color:#fff; border:1px solid #2e2e3d; padding:12px; border-radius:10px; font-family:inherit; }
                button { background:linear-gradient(135deg,#7c5cff,#ff5c8d); color:#fff; border:none; padding:13px; border-radius:10px; font-weight:700; cursor:pointer; }
                .feedback { margin-top:12px; color:#2ce5b5; font-weight:600; }
            """.trimIndent(),
            jsCode = """
                function sendBrief(e) {
                  e.preventDefault();
                  const name = document.getElementById('nameInput').value;
                  document.getElementById('formFeedback').textContent = 'Merci ' + name + ' ! Votre brief a été transmis au studio.';
                  e.target.reset();
                }
            """.trimIndent()
        ),
        WebTemplate(
            id = "tpl_restaurant",
            title = "L'Étoile Gastronomie",
            subtitle = "Site de restaurant gastronomique avec carte filtrable et réservation de table instantanée",
            category = ProjectCategory.RESTAURANT,
            badge = "Réservation",
            htmlCode = """
                <section class="hero">
                  <div class="inner">
                    <span>PARIS • GASTRONOMIE CONTEMPORAINE</span>
                    <h1>L'Étoile du Palais</h1>
                    <p>Une cuisine d'auteur guidée par les saisons, entre terre et mer.</p>
                    <a href="#reserve" class="gold-btn">Réserver une table</a>
                  </div>
                </section>
                <section class="menu wrap">
                  <h2>La Carte de Saison</h2>
                  <div class="tabs">
                    <button class="tab active" onclick="showCourse('entrees', this)">Entrées</button>
                    <button class="tab" onclick="showCourse('plats', this)">Plats</button>
                    <button class="tab" onclick="showCourse('desserts', this)">Desserts</button>
                  </div>
                  <div id="menuList" class="menu-list"></div>
                </section>
                <section id="reserve" class="wrap reserve">
                  <h2>Réserver votre table</h2>
                  <form onsubmit="bookTable(event)">
                    <input type="text" required placeholder="Nom de réservation" id="guestName" />
                    <input type="number" min="1" max="12" value="2" required id="guestCount" />
                    <input type="date" required id="bookDate" />
                    <button type="submit" class="gold-btn">Confirmer la réservation</button>
                  </form>
                  <p id="bookStatus"></p>
                </section>
            """.trimIndent(),
            cssCode = """
                * { box-sizing:border-box; margin:0; padding:0; }
                body { font-family:Georgia,serif; background:#14110f; color:#f6efe6; }
                .wrap { max-width:860px; margin:0 auto; padding:44px 20px; }
                .hero { min-height:360px; display:flex; align-items:center; justify-content:center; text-align:center; background:radial-gradient(circle at top,#38291e,#14110f); padding:40px 20px; }
                .hero span { color:#d8b26e; font-family:system-ui,sans-serif; font-size:0.8rem; letter-spacing:0.2em; }
                .hero h1 { font-size:clamp(2.2rem,5vw,3.8rem); margin:12px 0; }
                .hero p { color:#cbbba8; margin-bottom:24px; }
                .gold-btn { display:inline-block; background:#d8b26e; color:#14110f; text-decoration:none; border:none; padding:12px 24px; border-radius:8px; font-weight:700; font-family:system-ui,sans-serif; cursor:pointer; }
                h2 { font-size:1.9rem; margin-bottom:20px; color:#d8b26e; }
                .tabs { display:flex; gap:10px; margin-bottom:20px; }
                .tab { background:#211b17; color:#cbbba8; border:1px solid #3a3028; padding:8px 18px; border-radius:999px; cursor:pointer; font-family:system-ui,sans-serif; }
                .tab.active { background:#d8b26e; color:#14110f; border-color:#d8b26e; font-weight:700; }
                .menu-list { display:grid; gap:12px; }
                .dish { background:#1d1814; border:1px solid #302822; padding:18px; border-radius:12px; display:flex; justify-content:space-between; gap:16px; }
                .dish small { display:block; color:#b3a392; font-family:system-ui,sans-serif; margin-top:4px; }
                .reserve { background:#1d1814; border-radius:18px; border:1px solid #302822; margin-bottom:40px; }
                form { display:grid; grid-template-columns:repeat(auto-fit,minmax(160px,1fr)); gap:12px; }
                input { background:#14110f; color:#fff; border:1px solid #3a3028; padding:12px; border-radius:8px; font-family:system-ui,sans-serif; }
                #bookStatus { margin-top:14px; color:#2ce5b5; font-family:system-ui,sans-serif; }
            """.trimIndent(),
            jsCode = """
                const menu = {
                  entrees: [
                    { name:'Saint-Jacques snackées', desc:'Mousseline de panais & huile de truffe noire', price:'32 €' },
                    { name:'Foie gras mi-cuit', desc:'Chutney de figues rôties et brioche toastée', price:'29 €' }
                  ],
                  plats: [
                    { name:'Filet de bœuf Rossini', desc:'Jus corsé aux morilles et millefeuille de pommes de terre', price:'54 €' },
                    { name:'Turbot sauvage rôti', desc:'Beurre blanc au champagne et asperges vertes', price:'48 €' }
                  ],
                  desserts: [
                    { name:'Soufflé Grand Marnier', desc:'Glace vanille Bourbon de Madagascar', price:'21 €' },
                    { name:'Sphère chocolat grand cru', desc:'Cœur coulant praliné noisette', price:'22 €' }
                  ]
                };
                function showCourse(course, btn) {
                  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
                  if (btn) btn.classList.add('active');
                  document.getElementById('menuList').innerHTML = menu[course].map(d => `
                    <div class="dish"><div><strong>${'$'}{d.name}</strong><small>${'$'}{d.desc}</small></div><strong>${'$'}{d.price}</strong></div>
                  `).join('');
                }
                function bookTable(e) {
                  e.preventDefault();
                  const name = document.getElementById('guestName').value;
                  const count = document.getElementById('guestCount').value;
                  document.getElementById('bookStatus').textContent = 'Table confirmée pour ' + count + ' personne(s) au nom de ' + name + '.';
                  e.target.reset();
                }
                showCourse('entrees');
            """.trimIndent()
        )
    )
}
