import { expect, test } from '@playwright/test'

test('login is keyboard-accessible and validates required fields', async ({ page }) => {
  await page.goto('/login')

  await expect(page.getByRole('heading', { name: 'Bienvenido de vuelta' })).toBeVisible()
  await expect(page.getByLabel('Correo')).toHaveAttribute('autocomplete', 'email')
  await page.getByRole('button', { name: 'Ingresar' }).click()

  await expect(page.getByText('Ingresa tu correo')).toBeVisible()
  await expect(page.getByText(/Ingresa tu contrase/)).toBeVisible()
})

test('registration exposes customer and professional paths', async ({ page }) => {
  await page.goto('/registro')

  await expect(page.getByRole('heading', { name: 'Crea tu espacio' })).toBeVisible()
  await expect(page.getByRole('radio', { name: 'Profesional' })).toBeChecked()
  await page.getByRole('radio', { name: 'Cliente' }).check()
  await expect(page.getByRole('radio', { name: 'Cliente' })).toBeChecked()
  await expect(page.getByRole('button', { name: 'Crear cuenta' })).toBeVisible()
})
