# Odoo-Prestashop-XML
Este proyecto es una herramienta de integración en Java diseñada para automatizar la sincronización de productos entre Odoo ERP y PrestaShop. El sistema permite centralizar la gestión de datos en el ERP y reflejar los cambios en la tienda online de forma controlada.
Funcionalidades clave

    Sincronización de Productos: Busca productos por referencia y decide si crear un nuevo registro o actualizar el existente en PrestaShop.

    Gestión de Inventario: Calcula el stock real en Odoo sumando las variantes y actualiza el recurso stock_available mediante la API XML.

    Jerarquía de Categorías: Mapea y asigna el árbol completo de categorías desde Odoo para mantener la organización del catálogo.

    Procesamiento de Imágenes: Decodifica imágenes en formato Base64 desde el ERP y las sube automáticamente al servidor mediante peticiones multipart.

    Interfaz de Consola: Incluye un menú interactivo para listar productos disponibles y seleccionar manualmente el ID que se desea procesar.

Componentes técnicos

    Comunicación: Uso de HttpsURLConnection para realizar peticiones REST (GET, POST, PUT) a los Web Services.

    Formatos: Procesamiento de respuestas en JSON para la lectura de datos de Odoo y generación de documentos XML para la escritura en PrestaShop.

    Seguridad: Implementación de un cliente SSL que permite conexiones con certificados no verificados para entornos de desarrollo.
