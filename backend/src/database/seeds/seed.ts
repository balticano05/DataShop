import argon2 from 'argon2';
import dataSource from '../data-source.js';
import { Product } from '../entities/product.entity.js';
import { PromoCode } from '../entities/promo-code.entity.js';
import { User } from '../entities/user.entity.js';
import { UserRole } from '../enums/user-role.enum.js';

const ADMIN_EMAIL = 'admin@datashop.local';
const ADMIN_PASSWORD = process.env.SEED_ADMIN_PASSWORD ?? 'Admin12345!';

interface DemoProduct {
  title: string;
  description: string;
  price: number;
  rating: number;
  stockQuantity: number;
  label: string;
  color: string;
}

const datasetImage = (label: string, color: string, variant?: string): string =>
  `https://placehold.co/800x600/${color}/ffffff/png?text=${encodeURIComponent(
    variant ? `${label}\n${variant}` : label,
  )}&font=montserrat`;

const DEMO_PRODUCTS: DemoProduct[] = [
  { title: 'Акции Tesla 2019–2024', description: 'Дневные котировки TSLA с объёмами и дивидендами.', price: 1499.0, rating: 4.8, stockQuantity: 25, label: 'TSLA', color: '1e293b' },
  { title: 'Крипто-пары BTC/ETH 1 минута', description: 'Минутные свечи биржи за три года, 12 инструментов.', price: 2499.0, rating: 4.9, stockQuantity: 15, label: 'BTC / ETH', color: '6d28d9' },
  { title: 'Рынок недвижимости США', description: 'Цены, ставки и объёмы сделок по округам за 10 лет.', price: 3990.0, rating: 4.5, stockQuantity: 10, label: 'REAL ESTATE', color: '0f766e' },
  { title: 'Логи веб-сервера Nginx', description: 'Размеченные access-логи за месяц, 5 млн строк.', price: 899.0, rating: 4.2, stockQuantity: 40, label: 'NGINX LOGS', color: '9a3412' },
  { title: 'Отзывы Amazon (электроника)', description: 'Обезличенные отзывы и оценки, 500k записей.', price: 1290.0, rating: 4.4, stockQuantity: 30, label: 'AMAZON REVIEWS', color: 'b45309' },
  { title: 'Погода по 100 городам', description: 'Почасовые наблюдения: температура, давление, ветер.', price: 690.0, rating: 4.1, stockQuantity: 50, label: 'WEATHER', color: '0369a1' },
  { title: 'Авиарейсы и задержки', description: 'Расписание и фактические времена вылета за 2024.', price: 2190.0, rating: 4.6, stockQuantity: 12, label: 'FLIGHTS', color: '1d4ed8' },
  { title: 'Вакансии IT (HeadHunter)', description: 'Названия, грейды и вилки зарплат, 300k вакансий.', price: 1990.0, rating: 4.7, stockQuantity: 18, label: 'IT VACANCIES', color: '4338ca' },
  { title: 'Медицинские снимки (демо)', description: 'Анонимизированный набор рентген-снимков с разметкой.', price: 4990.0, rating: 4.9, stockQuantity: 6, label: 'MEDICAL IMAGING', color: '9f1239' },
  { title: 'Транзакции ритейла', description: 'Чеки супермаркета: товары, суммы, время покупки.', price: 1590.0, rating: 4.3, stockQuantity: 22, label: 'RETAIL', color: '15803d' },
  { title: 'Курсы валют ЦБ', description: 'Ежедневные курсы 30 валют за 15 лет.', price: 490.0, rating: 4.0, stockQuantity: 60, label: 'FX RATES', color: '7c2d12' },
  { title: 'Соцсеть: граф друзей', description: 'Обезличенный граф связей, 1 млн рёбер.', price: 2790.0, rating: 4.5, stockQuantity: 9, label: 'SOCIAL GRAPH', color: '5b21b6' },
  { title: 'Музыкальные треки и жанры', description: 'Аудио-фичи и жанровая разметка 200k треков.', price: 1890.0, rating: 4.6, stockQuantity: 14, label: 'MUSIC', color: 'a21caf' },
  { title: 'Дорожный трафик (датчики)', description: 'Интенсивность потока по 500 датчикам за год.', price: 3290.0, rating: 4.4, stockQuantity: 8, label: 'TRAFFIC', color: 'b91c1c' },
  { title: 'Футбольная статистика', description: 'Матчи, голы и xG по 20 лигам за 10 сезонов.', price: 2590.0, rating: 4.8, stockQuantity: 11, label: 'FOOTBALL', color: '166534' },
  { title: 'Энергопотребление домохозяйств', description: 'Почасовые показания счётчиков, 200 домов.', price: 1190.0, rating: 4.2, stockQuantity: 20, label: 'ENERGY', color: 'ca3500' },
  { title: 'Опросы общественного мнения', description: 'Анонимизированные ответы 50k респондентов.', price: 990.0, rating: 4.1, stockQuantity: 35, label: 'SURVEYS', color: '0e7490' },
  { title: 'Логи приложений (APM)', description: 'Трассировки и ошибки распределённой системы.', price: 3490.0, rating: 4.7, stockQuantity: 7, label: 'APM LOGS', color: '334155' },
];

async function seed(): Promise<void> {
  await dataSource.initialize();

  try {
    const userRepository = dataSource.getRepository(User);
    const existingAdmin = await userRepository.findOne({
      where: { email: ADMIN_EMAIL },
    });

    if (existingAdmin) {
      console.log(`admin ${ADMIN_EMAIL} already exists, skipped`);
    } else {
      const passwordHash = await argon2.hash(ADMIN_PASSWORD);
      await userRepository.save(
        userRepository.create({
          email: ADMIN_EMAIL,
          passwordHash,
          role: UserRole.Admin,
        }),
      );
      console.log(`created admin ${ADMIN_EMAIL}`);
    }

    const promoRepository = dataSource.getRepository(PromoCode);
    const existingPromo = await promoRepository.findOne({
      where: { code: 'SAVE10' },
    });

    if (existingPromo) {
      console.log('promo SAVE10 already exists, skipped');
    } else {
      await promoRepository.save(
        promoRepository.create({
          code: 'SAVE10',
          discountPercentage: 10,
          isActive: true,
        }),
      );
      console.log('created promo SAVE10');
    }

    const productRepository = dataSource.getRepository(Product);
    let createdProducts = 0;

    for (const [index, demo] of DEMO_PRODUCTS.entries()) {
      const exists = await productRepository.findOne({
        where: { title: demo.title },
      });
      if (exists) {
        continue;
      }

      await productRepository.save(
        productRepository.create({
          title: demo.title,
          description: demo.description,
          price: demo.price,
          rating: demo.rating,
          stockQuantity: demo.stockQuantity,
          filePublicId: `datashop/datasets/demo-${index + 1}`,
          images: [
            datasetImage(demo.label, demo.color),
            datasetImage(demo.label, demo.color, 'PREVIEW 1'),
            datasetImage(demo.label, demo.color, 'PREVIEW 2'),
          ],
        }),
      );
      createdProducts += 1;
    }

    console.log(
      `created ${createdProducts} products (${DEMO_PRODUCTS.length} total demo)`,
    );
  } finally {
    await dataSource.destroy();
  }
}

void seed();
